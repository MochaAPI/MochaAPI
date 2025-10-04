package com.mochaapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping HTTP POST requests onto specific handler methods.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PostMapping {
    
    /**
     * The path mapping URIs (e.g. "/myPath.do").
     * Ant-style path patterns are also supported (e.g. "/myPath/*.do").
     * At the method level, relative paths (e.g. "edit.do") are supported
     * within the primary mapping expressed at the type level.
     * Path mapping URIs may contain placeholders (e.g. "/${connect}").
     */
    String[] value() default {};
    
    /**
     * HTTP request methods to map to, narrowing the primary mapping:
     * GET, POST, HEAD, OPTIONS, PUT, PATCH, DELETE, TRACE.
     */
    String[] method() default {};
    
    /**
     * The parameters of the mapped request, narrowing the primary mapping.
     */
    String[] params() default {};
    
    /**
     * The headers of the mapped request, narrowing the primary mapping.
     */
    String[] headers() default {};
    
    /**
     * The consumable media types of the mapped request, narrowing the primary mapping.
     */
    String[] consumes() default {};
    
    /**
     * The producible media types of the mapped request, narrowing the primary mapping.
     */
    String[] produces() default {};
}
