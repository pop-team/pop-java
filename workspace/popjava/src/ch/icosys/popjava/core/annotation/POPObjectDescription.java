package ch.icosys.popjava.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.icosys.popjava.core.baseobject.ConnectionType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface POPObjectDescription {
	
	String LOCAL_DEBUG_URL = "localhost-debug";
	
	/**
	 * &lt;url&gt; or &lt;url&gt;:&lt;port&gt; where the object should be.
	 * If multiple protocol are active you should also define {@link #protocols()}
	 * @return the url of the object
	 */
	String url() default "";
	
	/**
	 * JVM parameters to be used when creating this object
	 * @return custom jvm parameters
	 */
	String jvmParameters() default "";
	
	/**
	 * The type of connection to be used to the remote host if the object
	 * has to be created remotely
	 * @return which type of connection should we use
	 */
	ConnectionType connection() default ConnectionType.ANY;
	
	String connectionSecret() default "";
	
	Encoding encoding() default Encoding.Default;
	
	/**
	 * Set to true to make the object automatically available over UPNP
	 */
	boolean upnp() default false;
	
	/**
	 * Enable method usage tracking on this method by user.
	 * @return is tracking enabled
	 */
	boolean tracking() default false;
	
	/**
	 * Don't spawn a new process when creating this object.
	 * @return is a local jvm object
	 */
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
	 * @return the protocol specified
	 */
	String[] protocols() default "";
	
	/**
	 * A network available on this machine
	 * @return the network we are working into
	 */
	String network() default "";
	
	/**
	 * An available connector present in a network
	 *  jobmanager: contact remote machine via the jobmanager
	 *  direct: connect directly (ex SSH) to the remote machine
	 * @return which type of connector was specified
	 */
	String connector() default "";
	
	/**
	 * Power requested
	 * @return how much power
	 */
	float power() default -1;
	/**
	 * Minimum power necessary
	 * @return how much power
	 */
	float minPower() default -1;
	/**
	 * Memory requested
	 * @return how much memory
	 */
	float memory() default -1;
	/**
	 * Minimum memory necessary
	 * @return how much memory
	 */
	float minMemory() default -1;
	/**
	 * Bandwidth requested
	 * @return how much bandwidth
	 */
	float bandwidth() default -1;
	/**
	 * Minimum bandwidth necessary
	 * @return how much bandwidth
	 */
	float minBandwidth() default -1;
	
	/**
	 * How many hops can we go after ours
	 * @return how many nodes should we traverse
	 */
	int searchDepth() default -1;
	
	/**
	 * How much time elapse between the start and end of a research in the grid.
	 * if 0 the first node found is used.
	 * any other value bigger than 0 (ms) will be a forced wait.
	 * @return how much time should we wait for answers
	 */
	int searchTime() default -1;
	
	/**
	 * Method id of the constructor.
	 * Only use this if you absolutely know what you are doing.
	 * @return the id
	 */
	int id() default -1;
}
