package com.mochaapi.runtime.server;

import com.mochaapi.runtime.MochaAPIConfig;
import com.mochaapi.runtime.router.Router;
import com.mochaapi.runtime.executor.ExecutorManager;
import com.mochaapi.runtime.context.RequestContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Netty-based HTTP server for MochaAPI.
 */
public class MochaServer {
    
    private static final Logger logger = LoggerFactory.getLogger(MochaServer.class);
    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
    
    private final MochaAPIConfig config;
    private final Router router;
    private final ExecutorManager executorManager;
    private final MeterRegistry meterRegistry;
    private final Counter requestCounter;
    private final AtomicLong activeConnections = new AtomicLong(0);
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    public MochaServer(MochaAPIConfig config, Router router, ExecutorManager executorManager) {
        this.config = config;
        this.router = router;
        this.executorManager = executorManager;
        this.meterRegistry = new SimpleMeterRegistry();
        this.requestCounter = Counter.builder("mochaapi_requests_total")
            .description("Total number of requests")
            .register(meterRegistry);
    }
    
    /**
     * Start the server.
     * 
     * @return a CompletableFuture that completes when the server is ready
     */
    public CompletableFuture<Void> start() {
        CompletableFuture<Void> startFuture = new CompletableFuture<>();
        
        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(1048576));
                        pipeline.addLast(new MochaRequestHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            ChannelFuture bindFuture = bootstrap.bind(config.getHost(), config.getPort());
            bindFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    serverChannel = future.channel();
                    logger.info("MochaAPI server started on {}:{}", config.getHost(), config.getPort());
                    startFuture.complete(null);
                } else {
                    logger.error("Failed to start MochaAPI server", future.cause());
                    startFuture.completeExceptionally(future.cause());
                }
            });
            
        } catch (Exception e) {
            startFuture.completeExceptionally(e);
        }
        
        return startFuture;
    }
    
    /**
     * Stop the server.
     */
    public void stop() {
        logger.info("Stopping MochaAPI server...");
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        executorManager.shutdown();
        logger.info("MochaAPI server stopped");
    }
    
    private class MochaRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        
        private static final Logger logger = LoggerFactory.getLogger(MochaRequestHandler.class);
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            activeConnections.incrementAndGet();
            requestCounter.increment();
            
            logger.debug("Processing {} request to {}", request.method(), request.uri());
            
            try {
                // Create request context
                RequestContext context = createRequestContext(request);
                
                // Handle request asynchronously
                router.handleRequest(context, executorManager)
                    .thenAccept(response -> {
                        try {
                            writeResponse(ctx, request, response);
                        } catch (Exception e) {
                            writeErrorResponse(ctx, request, e);
                        }
                    })
                    .exceptionally(throwable -> {
                        writeErrorResponse(ctx, request, throwable);
                        return null;
                    })
                    .whenComplete((result, throwable) -> {
                        activeConnections.decrementAndGet();
                    });
                    
            } catch (Exception e) {
                logger.error("Error processing request to {}", request.uri(), e);
                writeErrorResponse(ctx, request, e);
                activeConnections.decrementAndGet();
            }
        }
        
        private RequestContext createRequestContext(FullHttpRequest request) {
            RequestContext context = new RequestContext();
            context.setMethod(request.method().name());
            
            // Parse URI to extract path and query parameters
            String uri = request.uri();
            String path = uri;
            String queryString = "";
            
            int queryIndex = uri.indexOf('?');
            if (queryIndex != -1) {
                path = uri.substring(0, queryIndex);
                queryString = uri.substring(queryIndex + 1);
            }
            
            context.setPath(path);
            context.setContentType(request.headers().get(HttpHeaderNames.CONTENT_TYPE));
            
            // Parse query parameters
            if (!queryString.isEmpty()) {
                String[] params = queryString.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2) {
                        context.setQueryParam(keyValue[0], keyValue[1]);
                    }
                }
            }
            
            // Set headers
            for (String name : request.headers().names()) {
                context.setHeader(name, request.headers().get(name));
            }
            
            // Set body
            if (request.content() != null && request.content().readableBytes() > 0) {
                context.setBody(request.content().toString(io.netty.util.CharsetUtil.UTF_8));
            }
            
            return context;
        }
        
        private void writeResponse(ChannelHandlerContext ctx, FullHttpRequest request, Object response) {
            FullHttpResponse httpResponse;
            
            if (response instanceof String) {
                httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    io.netty.buffer.Unpooled.copiedBuffer((String) response, io.netty.util.CharsetUtil.UTF_8)
                );
                httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            } else {
                // Serialize object to JSON using Jackson
                try {
                    String jsonResponse = OBJECT_MAPPER.writeValueAsString(response);
                    httpResponse = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        io.netty.buffer.Unpooled.copiedBuffer(jsonResponse, io.netty.util.CharsetUtil.UTF_8)
                    );
                    httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                } catch (Exception e) {
                    // Fallback to toString if JSON serialization fails
                    String jsonResponse = "{\"data\":\"" + response.toString() + "\"}";
                    httpResponse = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        io.netty.buffer.Unpooled.copiedBuffer(jsonResponse, io.netty.util.CharsetUtil.UTF_8)
                    );
                    httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                }
            }
            
            httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            
            ctx.writeAndFlush(httpResponse);
        }
        
        private void writeErrorResponse(ChannelHandlerContext ctx, FullHttpRequest request, Throwable error) {
            String errorMessage = "{\"error\":\"" + error.getMessage() + "\"}";
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                io.netty.buffer.Unpooled.copiedBuffer(errorMessage, io.netty.util.CharsetUtil.UTF_8)
            );
            
            httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            
            ctx.writeAndFlush(httpResponse);
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
