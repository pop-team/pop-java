package ch.icosys.popjava.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a method as asynchronous and sequential.
 * Asynchronous method calls do not wait for the call to finish and continue
 * execution of the calling code directly. Asynchronous methods can't have a return value.
 * Sequential methods can only be executed when all other sequential and mutex calls on the same
 * object are terminated. Concurrent calls do not restrict sequential methods.
 * @author Beat Wolf
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@POPSemantic
public @interface POPAsyncSeq {
    int id() default -1;
	boolean localhost() default false;
}
