package popjava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import popjava.baseobject.ConnectionType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface POPObjectDescription {
	
	public static final String LOCAL_DEBUG_URL = "localhost-debug";
	
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
	
	String connectionSecret() default "";
	
	Encoding encoding() default Encoding.Default;
	
	/**
	 * Method id of the constructor.
	 * Only use this if you absolutely know what you are doing.
	 * @return
	 */
	int id() default -1;
}
