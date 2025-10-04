package com.mochaapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating a method parameter should be bound to the body of the web request.
 * The body of the request is passed through an HttpMessageConverter to resolve the
 * method argument depending on the content type of the request.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
    
    /**
     * Whether body content is required.
     * Default is true, leading to an exception thrown in case
     * there is no body content. Switch this to false if you prefer
     * null to be passed when the body content is null.
     */
    boolean required() default true;
}
