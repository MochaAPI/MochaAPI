package com.mochaapi.runtime.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RequestContext functionality.
 */
public class RequestContextTest {
    
    private RequestContext context;
    
    @BeforeEach
    public void setUp() {
        context = new RequestContext();
    }
    
    @Test
    public void testBasicProperties() {
        context.setMethod("GET");
        context.setPath("/test");
        context.setContentType("application/json");
        
        assertEquals("GET", context.getMethod());
        assertEquals("/test", context.getPath());
        assertEquals("application/json", context.getContentType());
    }
    
    @Test
    public void testHeaders() {
        context.setHeader("User-Agent", "MochaAPI-Test");
        context.setHeader("Content-Type", "application/json");
        
        assertEquals("MochaAPI-Test", context.getHeader("User-Agent"));
        assertEquals("application/json", context.getHeader("Content-Type"));
        assertNull(context.getHeader("Non-Existent"));
    }
    
    @Test
    public void testQueryParams() {
        context.setQueryParam("page", "1");
        context.setQueryParam("size", "10");
        
        assertEquals("1", context.getQueryParam("page"));
        assertEquals("10", context.getQueryParam("size"));
        assertNull(context.getQueryParam("non-existent"));
    }
    
    @Test
    public void testPathParams() {
        context.setPathParam("id", "123");
        context.setPathParam("name", "test");
        
        assertEquals("123", context.getPathParam("id"));
        assertEquals("test", context.getPathParam("name"));
        assertNull(context.getPathParam("non-existent"));
    }
    
    @Test
    public void testBody() {
        String jsonBody = "{\"name\":\"test\",\"value\":123}";
        context.setBody(jsonBody);
        
        assertEquals(jsonBody, context.getBody());
    }
}
