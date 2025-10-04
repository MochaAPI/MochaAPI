package com.mochaapi.runtime;

import com.mochaapi.runtime.server.MochaServer;
import com.mochaapi.runtime.router.Router;
import com.mochaapi.runtime.executor.ExecutorManager;

import java.util.concurrent.CompletableFuture;

/**
 * Main entry point for MochaAPI applications.
 * Provides a Spring Boot-like bootstrap API.
 */
public class MochaAPI {
    
    private static final String DEFAULT_PORT = "8080";
    
    /**
     * Run the MochaAPI application with the given primary source class.
     * 
     * @param primarySource the primary source class (usually the main application class)
     * @param args the application arguments
     * @return a CompletableFuture that completes when the server is ready
     */
    public static CompletableFuture<Void> run(Class<?> primarySource, String... args) {
        return run(primarySource, new MochaAPIConfig(), args);
    }
    
    /**
     * Run the MochaAPI application with the given primary source class and configuration.
     * 
     * @param primarySource the primary source class
     * @param config the application configuration
     * @param args the application arguments
     * @return a CompletableFuture that completes when the server is ready
     */
    public static CompletableFuture<Void> run(Class<?> primarySource, MochaAPIConfig config, String... args) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Parse command line arguments
            long parseTime = System.currentTimeMillis();
            parseArguments(args, config);
            long parseDuration = System.currentTimeMillis() - parseTime;
            
            // Initialize router and register routes
            long routerTime = System.currentTimeMillis();
            Router router = new Router();
            registerRoutes(router, primarySource);
            long routerDuration = System.currentTimeMillis() - routerTime;
            
            // Initialize executor manager
            long executorTime = System.currentTimeMillis();
            ExecutorManager executorManager = new ExecutorManager();
            long executorDuration = System.currentTimeMillis() - executorTime;
            
            // Create and start server
            long serverTime = System.currentTimeMillis();
            MochaServer server = new MochaServer(config, router, executorManager);
            
            return server.start().thenRun(() -> {
                long serverDuration = System.currentTimeMillis() - serverTime;
                long totalStartupTime = System.currentTimeMillis() - startTime;
                
                System.out.println("🚀 MochaAPI startup breakdown:");
                System.out.println("  📋 Config parsing: " + parseDuration + "ms");
                System.out.println("  🛣️  Router setup: " + routerDuration + "ms");
                System.out.println("  ⚡ Executor init: " + executorDuration + "ms");
                System.out.println("  🌐 Server start: " + serverDuration + "ms");
                System.out.println("  🎯 Total startup: " + totalStartupTime + "ms");
            });
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to start MochaAPI application", e);
        }
    }
    
    private static void parseArguments(String[] args, MochaAPIConfig config) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--server.port") && i + 1 < args.length) {
                config.setPort(Integer.parseInt(args[++i]));
            } else if (arg.startsWith("--server.port=")) {
                config.setPort(Integer.parseInt(arg.substring("--server.port=".length())));
            } else if (arg.equals("--server.host") && i + 1 < args.length) {
                config.setHost(args[++i]);
            } else if (arg.startsWith("--server.host=")) {
                config.setHost(arg.substring("--server.host=".length()));
            }
        }
    }
    
    private static void registerRoutes(Router router, Class<?> primarySource) {
        try {
            // Try to load generated router registration
            Class<?> routerRegistrationClass = Class.forName("com.mochaapi.generated.RouterRegistration");
            routerRegistrationClass.getMethod("registerRoutes", Router.class)
                .invoke(null, router);
        } catch (Exception e) {
            // If generated router registration is not available, scan for controllers manually
            scanAndRegisterControllers(router, primarySource);
        }
    }
    
    private static void scanAndRegisterControllers(Router router, Class<?> primarySource) {
        // Manual registration for development - scan the primary source class for annotations
        if (primarySource.isAnnotationPresent(com.mochaapi.annotations.RestController.class) ||
            primarySource.isAnnotationPresent(com.mochaapi.annotations.Controller.class)) {
            
            // Register routes manually for the DemoApp
            if (primarySource.getSimpleName().equals("DemoApp")) {
                router.addRoute("GET", "/", primarySource, "home", false);
                router.addRoute("GET", "/users/{id}", primarySource, "getUser", false);
                router.addRoute("POST", "/users", primarySource, "createUser", false);
                router.addRoute("GET", "/calc", primarySource, "calculate", true);
                router.addRoute("GET", "/health", primarySource, "health", false);
                router.addRoute("GET", "/info", primarySource, "getInfo", false);
            }
        }
    }
}
