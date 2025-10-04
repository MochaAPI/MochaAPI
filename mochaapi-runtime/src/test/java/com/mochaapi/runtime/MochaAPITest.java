package com.mochaapi.runtime;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for MochaAPI runtime.
 */
public class MochaAPITest {
    
    @Test
    public void testMochaAPIConfigDefaultValues() {
        MochaAPIConfig config = new MochaAPIConfig();
        
        assertEquals("0.0.0.0", config.getHost());
        assertEquals(8080, config.getPort());
        assertTrue(config.isEnableMetrics());
        assertTrue(config.isEnableHealthCheck());
        assertTrue(config.isEnableOpenApi());
        assertEquals("/docs", config.getOpenApiPath());
    }
    
    @Test
    public void testMochaAPIConfigSetters() {
        MochaAPIConfig config = new MochaAPIConfig();
        
        config.setHost("localhost");
        config.setPort(9090);
        config.setEnableMetrics(false);
        config.setEnableHealthCheck(false);
        config.setEnableOpenApi(false);
        config.setOpenApiPath("/api-docs");
        
        assertEquals("localhost", config.getHost());
        assertEquals(9090, config.getPort());
        assertFalse(config.isEnableMetrics());
        assertFalse(config.isEnableHealthCheck());
        assertFalse(config.isEnableOpenApi());
        assertEquals("/api-docs", config.getOpenApiPath());
    }
}
