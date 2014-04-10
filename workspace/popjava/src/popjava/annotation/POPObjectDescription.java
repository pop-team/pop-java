package popjava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import popjava.baseobject.ConnectionType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface POPObjectDescription {
	
	String url() default "";
	
	/**
	 * JVM parameters to be used when creating this object
	 * @return
	 */
	String jvmParameters() default "";
	
	/**
	 * The type of connection to be used to the remote host if the object
	 * has to be created remotely
	 * @return
	 */
	ConnectionType connection() default ConnectionType.SSH;
	
}
