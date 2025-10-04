package com.mochaapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates that a method should be executed on a CPU-bound thread pool
 * optimized for compute-intensive tasks. This is particularly useful for CPU-heavy operations
 * that would block virtual threads.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CpuBound {
    
    /**
     * Optional priority for the CPU-bound task execution.
     * Higher values indicate higher priority.
     */
    int priority() default 0;
}
