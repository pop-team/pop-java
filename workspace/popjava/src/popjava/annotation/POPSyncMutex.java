package popjava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a method as synchronous and mutex.
 * Synchronous method calls wait for the call to finish before continuing the execution of the calling code.
 * Mutex methods can only be executed when no other method is executed on the same object.
 * @author Beat Wolf
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface POPSyncMutex {

}
