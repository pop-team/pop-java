package popjava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface POPObjectDescription {
	
	String url() default "";
	
	/**
	 * JVM parameters to be used when creating this object
	 * @return
	 */
	String jvmParameters() default "";
	
}
