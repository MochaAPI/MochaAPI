package com.mochaapi.examples;

import com.mochaapi.annotations.*;
import com.mochaapi.runtime.MochaAPI;

/**
 * Example MochaAPI application demonstrating the framework capabilities.
 */
@RestController
public class DemoApp {
    
    public static void main(String[] args) {
        MochaAPI.run(DemoApp.class, args);
    }
    
    @GetMapping("/")
    public String home() {
        return "Welcome to MochaAPI!";
    }
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable("id") String id) {
        return new User(id, "John Doe", "john.doe@example.com");
    }
    
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        // Simulate user creation with a generated ID
        String newId = String.valueOf(System.currentTimeMillis() % 10000);
        return new User(newId, user.getName(), user.getEmail());
    }
    
    @GetMapping("/calc")
    @CpuBound
    public CalcResult calculate(@RequestParam("a") int a, @RequestParam("b") int b) {
        // Simulate CPU-intensive calculation
        int result = 0;
        for (int i = 0; i < 1000000; i++) {
            result += a * b + i;
        }
        return new CalcResult(a, b, result);
    }
    
    @GetMapping("/health")
    public HealthStatus health() {
        return new HealthStatus("UP", System.currentTimeMillis());
    }
    
    @GetMapping("/info")
    public String getInfo(@RequestHeader("User-Agent") String userAgent) {
        return "User-Agent: " + userAgent;
    }
    
    // DTO classes
    public static class User {
        private String id;
        private String name;
        private String email;
        
        public User() {}
        
        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class CalcResult {
        private int a;
        private int b;
        private int result;
        
        public CalcResult() {}
        
        public CalcResult(int a, int b, int result) {
            this.a = a;
            this.b = b;
            this.result = result;
        }
        
        public int getA() { return a; }
        public void setA(int a) { this.a = a; }
        public int getB() { return b; }
        public void setB(int b) { this.b = b; }
        public int getResult() { return result; }
        public void setResult(int result) { this.result = result; }
    }
    
    public static class HealthStatus {
        private String status;
        private long timestamp;
        
        public HealthStatus() {}
        
        public HealthStatus(String status, long timestamp) {
            this.status = status;
            this.timestamp = timestamp;
        }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
