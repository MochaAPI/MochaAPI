package com.mochaapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates that a method parameter should be bound to a web request header.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestHeader {
    
    /**
     * The name of the request header to bind to.
     */
    String value() default "";
    
    /**
     * Whether the header is required.
     * Default is true, leading to an exception being thrown
     * if the header is missing in the request. Switch this to
     * false if you prefer null values if the header is not present.
     */
    boolean required() default true;
    
    /**
     * The default value to use as a fallback when the request header is
     * not provided or has an empty value.
     */
    String defaultValue() default "";
}
