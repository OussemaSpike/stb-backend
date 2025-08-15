package com.pfe.stb.config.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method as public, meaning it should be excluded from Spring Security
 * authentication.
 */
@Target(ElementType.METHOD) // Applies to methods
@Retention(RetentionPolicy.RUNTIME) // Available at runtime for reflection
public @interface PublicEndpoint {}
