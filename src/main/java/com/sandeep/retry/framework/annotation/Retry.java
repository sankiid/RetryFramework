package com.sandeep.retry.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author sandeep
 * 
 *         Use this annotations if you want to retry a function in case of any
 *         kind of exceptions.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Retry {

	int attempts()

	default 2;

	long delay()

	default 100;

	TimeUnit unit() default TimeUnit.MILLISECONDS;
}
