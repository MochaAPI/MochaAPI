package com.mochaapi.examples;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DemoApp functionality.
 */
public class DemoAppTest {
    
    @Test
    public void testUserCreation() {
        DemoApp app = new DemoApp();
        
        DemoApp.User user = new DemoApp.User("1", "John Doe", "john@example.com");
        DemoApp.User createdUser = app.createUser(user);
        
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("John Doe", createdUser.getName());
        assertEquals("john@example.com", createdUser.getEmail());
    }
    
    @Test
    public void testUserRetrieval() {
        DemoApp app = new DemoApp();
        
        DemoApp.User user = app.getUser("123");
        
        assertNotNull(user);
        assertEquals("123", user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john.doe@example.com", user.getEmail());
    }
    
    @Test
    public void testHealthCheck() {
        DemoApp app = new DemoApp();
        
        DemoApp.HealthStatus health = app.health();
        
        assertNotNull(health);
        assertEquals("UP", health.getStatus());
        assertTrue(health.getTimestamp() > 0);
    }
    
    @Test
    public void testHomePage() {
        DemoApp app = new DemoApp();
        
        String home = app.home();
        
        assertNotNull(home);
        assertTrue(home.contains("MochaAPI"));
    }
    
    @Test
    public void testCalculation() {
        DemoApp app = new DemoApp();
        
        DemoApp.CalcResult result = app.calculate(5, 10);
        
        assertNotNull(result);
        assertEquals(5, result.getA());
        assertEquals(10, result.getB());
        assertTrue(result.getResult() > 0);
    }
    
    @Test
    public void testGetInfo() {
        DemoApp app = new DemoApp();
        
        String info = app.getInfo("MochaAPI-Test/1.0");
        
        assertNotNull(info);
        assertTrue(info.contains("User-Agent"));
        assertTrue(info.contains("MochaAPI-Test"));
    }
}
