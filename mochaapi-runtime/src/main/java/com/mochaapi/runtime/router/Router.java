package com.mochaapi.runtime.router;

import com.mochaapi.runtime.context.RequestContext;
import com.mochaapi.runtime.executor.ExecutorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Router for handling HTTP requests and mapping them to controller methods.
 */
public class Router {
    
    private static final Logger logger = LoggerFactory.getLogger(Router.class);
    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
    
    private final ConcurrentHashMap<String, RouteHandler> routes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Object> controllerInstances = new ConcurrentHashMap<>();
    
    /**
     * Add a route to the router.
     * 
     * @param method HTTP method (GET, POST, etc.)
     * @param path the path pattern
     * @param controllerClass the controller class
     * @param methodName the method name
     * @param isCpuBound whether this route should run on CPU-bound executor
     */
    public void addRoute(String method, String path, Class<?> controllerClass, String methodName, boolean isCpuBound) {
        String routeKey = method + " " + path;
        RouteHandler handler = new RouteHandler(controllerClass, methodName, isCpuBound);
        routes.put(routeKey, handler);
    }
    
    /**
     * Handle a request by routing it to the appropriate controller method.
     * 
     * @param context the request context
     * @param executorManager the executor manager
     * @return a CompletableFuture that completes with the response
     */
    public CompletableFuture<Object> handleRequest(RequestContext context, ExecutorManager executorManager) {
        String routeKey = context.getMethod() + " " + context.getPath();
        logger.debug("Looking for route: {}", routeKey);
        
        RouteHandler handler = routes.get(routeKey);
        
        // Try to find a route with path parameters
        if (handler == null) {
            handler = findRoute(context.getMethod(), context.getPath());
        }
        
        if (handler == null) {
            return CompletableFuture.completedFuture(createNotFoundResponse());
        }
        
        try {
            Object controller = getControllerInstance(handler.getControllerClass());
            Method method = findMethod(handler.getControllerClass(), handler.getMethodName());
            
            if (method == null) {
                return CompletableFuture.completedFuture(createNotFoundResponse());
            }
            
            // Prepare method arguments
            Object[] args = prepareMethodArguments(method, context);
            
            // Execute method
            if (handler.isCpuBound()) {
                return executorManager.executeCpuBound(() -> {
                    try {
                        return method.invoke(controller, args);
                    } catch (Exception e) {
                        throw new RuntimeException("Error executing controller method", e);
                    }
                });
            } else {
                return executorManager.executeVirtual(() -> {
                    try {
                        return method.invoke(controller, args);
                    } catch (Exception e) {
                        throw new RuntimeException("Error executing controller method", e);
                    }
                });
            }
            
        } catch (Exception e) {
            return CompletableFuture.completedFuture(createErrorResponse(e));
        }
    }
    
    private Object getControllerInstance(Class<?> controllerClass) {
        return controllerInstances.computeIfAbsent(controllerClass, clazz -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create controller instance", e);
            }
        });
    }
    
    private Method findMethod(Class<?> controllerClass, String methodName) {
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }
    
    private RouteHandler findRoute(String method, String path) {
        // Simple path parameter matching for development
        for (Map.Entry<String, RouteHandler> entry : routes.entrySet()) {
            String routeKey = entry.getKey();
            String[] parts = routeKey.split(" ", 2);
            if (parts.length == 2 && parts[0].equals(method)) {
                String routePath = parts[1];
                
                // Handle path parameters like /users/{id}
                if (routePath.contains("{") && routePath.contains("}")) {
                    if (matchesPathWithParams(routePath, path)) {
                        return entry.getValue();
                    }
                }
            }
        }
        return null;
    }
    
    private boolean matchesPathWithParams(String routePath, String requestPath) {
        // Simple path parameter matching
        // Convert /users/{id} to /users/.* and match
        String pattern = routePath.replaceAll("\\{[^}]+\\}", "[^/]+");
        return requestPath.matches(pattern);
    }
    
    private Object[] prepareMethodArguments(Method method, RequestContext context) {
        // Simple parameter binding for development
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            java.lang.reflect.Parameter param = parameters[i];
            String paramName = param.getName();
            Class<?> paramType = param.getType();
            
            // Handle @PathVariable
            if (param.isAnnotationPresent(com.mochaapi.annotations.PathVariable.class)) {
                com.mochaapi.annotations.PathVariable pathVariable = param.getAnnotation(com.mochaapi.annotations.PathVariable.class);
                String paramKey = pathVariable.value().isEmpty() ? paramName : pathVariable.value();
                // Extract path parameter from URL
                String pathValue = extractPathParameter(context.getPath(), paramKey);
                args[i] = convertToType(pathValue, paramType);
            }
            // Handle @RequestParam
            else if (param.isAnnotationPresent(com.mochaapi.annotations.RequestParam.class)) {
                com.mochaapi.annotations.RequestParam requestParam = param.getAnnotation(com.mochaapi.annotations.RequestParam.class);
                String paramKey = requestParam.value().isEmpty() ? paramName : requestParam.value();
                String queryValue = context.getQueryParam(paramKey);
                args[i] = convertToType(queryValue, paramType);
            }
            // Handle @RequestHeader
            else if (param.isAnnotationPresent(com.mochaapi.annotations.RequestHeader.class)) {
                com.mochaapi.annotations.RequestHeader requestHeader = param.getAnnotation(com.mochaapi.annotations.RequestHeader.class);
                String paramKey = requestHeader.value().isEmpty() ? paramName : requestHeader.value();
                String headerValue = context.getHeader(paramKey);
                args[i] = convertToType(headerValue, paramType);
            }
            // Handle @RequestBody
            else if (param.isAnnotationPresent(com.mochaapi.annotations.RequestBody.class)) {
                try {
                    String body = context.getBody();
                    if (body != null && !body.isEmpty()) {
                        args[i] = OBJECT_MAPPER.readValue(body, paramType);
                    } else {
                        args[i] = null;
                    }
                } catch (Exception e) {
                    // If JSON deserialization fails, return null
                    args[i] = null;
                }
            }
            else {
                args[i] = null;
            }
        }
        
        return args;
    }
    
    private String extractPathParameter(String path, String paramName) {
        // Simple path parameter extraction
        // For /users/123, extract 123
        String[] pathParts = path.split("/");
        if (pathParts.length > 2) {
            return pathParts[pathParts.length - 1];
        }
        return "";
    }
    
    private Object convertToType(String value, Class<?> type) {
        if (value == null) return null;
        
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else if (type == long.class || type == Long.class) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        
        return value;
    }
    
    private Object createNotFoundResponse() {
        return new ErrorResponse(404, "Not Found");
    }
    
    private Object createErrorResponse(Exception e) {
        return new ErrorResponse(500, "Internal Server Error: " + e.getMessage());
    }
    
    private static class RouteHandler {
        private final Class<?> controllerClass;
        private final String methodName;
        private final boolean isCpuBound;
        
        public RouteHandler(Class<?> controllerClass, String methodName, boolean isCpuBound) {
            this.controllerClass = controllerClass;
            this.methodName = methodName;
            this.isCpuBound = isCpuBound;
        }
        
        public Class<?> getControllerClass() {
            return controllerClass;
        }
        
        public String getMethodName() {
            return methodName;
        }
        
        public boolean isCpuBound() {
            return isCpuBound;
        }
    }
    
    private static class ErrorResponse {
        private final int status;
        private final String message;
        
        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }
        
        public int getStatus() {
            return status;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
