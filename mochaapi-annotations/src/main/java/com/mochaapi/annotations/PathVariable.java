package com.mochaapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates that a method parameter should be bound to a URI template variable.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
    
    /**
     * The name of the path variable to bind to.
     */
    String value() default "";
    
    /**
     * Whether the path variable is required.
     * Default is true, leading to an exception being thrown
     * if the path variable is missing in the request. Switch this to
     * false if you prefer null values if the path variable is not present.
     */
    boolean required() default true;
}
