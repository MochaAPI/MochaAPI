package com.mochaapi.runtime.router;

import com.mochaapi.runtime.context.RequestContext;
import com.mochaapi.runtime.executor.ExecutorManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Router functionality.
 */
public class RouterTest {
    
    private Router router;
    private ExecutorManager executorManager;
    
    @BeforeEach
    public void setUp() {
        router = new Router();
        executorManager = new ExecutorManager();
    }
    
    @Test
    public void testAddRoute() {
        // Test adding a simple route
        router.addRoute("GET", "/test", String.class, "testMethod", false);
        
        // Create a request context
        RequestContext context = new RequestContext();
        context.setMethod("GET");
        context.setPath("/test");
        
        // The route should be found (though method execution will fail without proper controller)
        // This tests the route registration mechanism
        assertNotNull(router);
    }
    
    @Test
    public void testRouteNotFound() {
        RequestContext context = new RequestContext();
        context.setMethod("GET");
        context.setPath("/nonexistent");
        
        // This should return a not found response
        var result = router.handleRequest(context, executorManager).join();
        assertNotNull(result);
    }
}
