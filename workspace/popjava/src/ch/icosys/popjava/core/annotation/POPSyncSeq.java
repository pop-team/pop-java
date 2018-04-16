package ch.icosys.popjava.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a method as synchronous and sequential.
 * Synchronous method calls wait for the call to finish before continuing the execution of the calling code.
 * Sequential methods can only be executed when all other sequential and mutex calls on the same
 * object are terminated. Concurrent calls do not restrict sequential methods.
 * @author Beat Wolf
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@POPSemantic
public @interface POPSyncSeq {
    int id() default -1;
	boolean localhost() default false;
}
