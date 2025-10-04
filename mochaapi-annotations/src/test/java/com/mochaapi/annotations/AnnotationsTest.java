package com.mochaapi.annotations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for annotation functionality.
 */
public class AnnotationsTest {
    
    @Test
    public void testRestControllerAnnotation() {
        RestController annotation = TestController.class.getAnnotation(RestController.class);
        assertNotNull(annotation);
    }
    
    @Test
    public void testGetMappingAnnotation() {
        try {
            var method = TestController.class.getMethod("testGet");
            GetMapping annotation = method.getAnnotation(GetMapping.class);
            assertNotNull(annotation);
            assertEquals("/test", annotation.value()[0]);
        } catch (NoSuchMethodException e) {
            fail("Method not found: " + e.getMessage());
        }
    }
    
    @Test
    public void testPostMappingAnnotation() {
        try {
            var method = TestController.class.getMethod("testPost", String.class);
            PostMapping annotation = method.getAnnotation(PostMapping.class);
            assertNotNull(annotation);
            assertEquals("/test", annotation.value()[0]);
        } catch (NoSuchMethodException e) {
            fail("Method not found: " + e.getMessage());
        }
    }
    
    @Test
    public void testRequestParamAnnotation() {
        try {
            var method = TestController.class.getMethod("testParam", String.class);
            var param = method.getParameters()[0];
            RequestParam annotation = param.getAnnotation(RequestParam.class);
            assertNotNull(annotation);
            assertEquals("name", annotation.value());
        } catch (NoSuchMethodException e) {
            fail("Method not found");
        }
    }
    
    @Test
    public void testRequestBodyAnnotation() {
        try {
            var method = TestController.class.getMethod("testBody", String.class);
            var param = method.getParameters()[0];
            RequestBody annotation = param.getAnnotation(RequestBody.class);
            assertNotNull(annotation);
        } catch (NoSuchMethodException e) {
            fail("Method not found");
        }
    }
    
    @Test
    public void testPathVariableAnnotation() {
        try {
            var method = TestController.class.getMethod("testPath", String.class);
            var param = method.getParameters()[0];
            PathVariable annotation = param.getAnnotation(PathVariable.class);
            assertNotNull(annotation);
            assertEquals("id", annotation.value());
        } catch (NoSuchMethodException e) {
            fail("Method not found");
        }
    }
    
    @Test
    public void testRequestHeaderAnnotation() {
        try {
            var method = TestController.class.getMethod("testHeader", String.class);
            var param = method.getParameters()[0];
            RequestHeader annotation = param.getAnnotation(RequestHeader.class);
            assertNotNull(annotation);
            assertEquals("User-Agent", annotation.value());
        } catch (NoSuchMethodException e) {
            fail("Method not found");
        }
    }
    
    @Test
    public void testCpuBoundAnnotation() {
        try {
            var method = TestController.class.getMethod("testCpuBound");
            CpuBound annotation = method.getAnnotation(CpuBound.class);
            assertNotNull(annotation);
        } catch (NoSuchMethodException e) {
            fail("Method not found");
        }
    }
    
    // Test controller class
    @RestController
    public static class TestController {
        
        @GetMapping("/test")
        public String testGet() {
            return "test";
        }
        
        @PostMapping("/test")
        public String testPost(@RequestBody String body) {
            return "test";
        }
        
        public String testParam(@RequestParam("name") String name) {
            return "test";
        }
        
        public String testBody(@RequestBody String body) {
            return "test";
        }
        
        public String testPath(@PathVariable("id") String id) {
            return "test";
        }
        
        public String testHeader(@RequestHeader("User-Agent") String userAgent) {
            return "test";
        }
        
        @CpuBound
        public String testCpuBound() {
            return "test";
        }
    }
}
