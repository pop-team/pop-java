package popjava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a method as asynchronous and concurrent.
 * Asynchronous method calls do not wait for the call to finish and continue
 * execution of the calling code directly. Asynchronous methods can't have a return value.
 * Concurrent methods can be called in parallel by multiple processes. They can not be executed
 * if a mutex call is currently being executed on the target object
 * @author Beat Wolf
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@POPSemantic
public @interface POPAsyncConc {
    int id() default -1;
	boolean localhost() default false;
}
