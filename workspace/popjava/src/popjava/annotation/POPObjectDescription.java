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
	
	/**
	 * <url> or <url>:<port> where the object should be.
	 * If multiple procol are active you should also define {@link #protocol()}
	 * @return 
	 */
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
	
	boolean localJVM() default false;
	
	/**
	 * Protocols this object should use.
	 * Options:
	 *   ""
	 *   "protocol"
	 *   "protocol:port"
	 *   {"protocol1", "protocol2"}
	 *   {"protocol1:port1", "protocol2"}
	 *   {"protocol1:port1", "protocol2:port2"}
	 * 
	 * If nothing is specified all available protocols will be used.
	 * @return 
	 */
	String[] protocols() default "";
	
	/**
	 * A network available on this machine
	 * @return 
	 */
	String network() default "";
	
	/**
	 * An available connector present in a network
	 *  jobmanager: contact remote machine via the jobmanager
	 *  direct: connect directly (ex SSH) to the remote machine
	 * @return 
	 */
	String connector() default "";
	
	/**
	 * Power requested
	 * @return 
	 */
	float power() default -1;
	/**
	 * Minimum power necessary
	 * @return 
	 */
	float minPower() default -1;
	/**
	 * Memory requested
	 * @return 
	 */
	float memory() default -1;
	/**
	 * Minimum memory necessary
	 * @return 
	 */
	float minMemory() default -1;
	/**
	 * Bandwidth requested
	 * @return 
	 */
	float bandwidth() default -1;
	/**
	 * Minimum bandwidth necessary
	 * @return 
	 */
	float minBandwidth() default -1;
	
	/**
	 * How many hops can we go after ours
	 * @return 
	 */
	int searchDepth() default -1;
	
	/**
	 * How much time elapse between the start and end of a research in the grid
	 * if 0 the first node found is used
	 * any other value > 0 (ms) will be a forced wait
	 * @return 
	 */
	int searchTime() default -1;
	
	/**
	 * Method id of the constructor.
	 * Only use this if you absolutely know what you are doing.
	 * @return
	 */
	int id() default -1;
}
