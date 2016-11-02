package popjava.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import popjava.broker.RequestQueue;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface POPClass {
	String className() default "";
	int classId() default -1;
	boolean deconstructor() default false;
	int maxRequestQueue() default RequestQueue.DEFAULT_REQUEST_QUEUE_SIZE;
	boolean isDistributable() default true;
	boolean useAsyncConstructor() default true;
}