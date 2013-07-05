package popjava.util;

/**
 * This class regroup some configuration values
 */
public class Configuration {

	/**
	 * Creates a new instance of POPConfiguration
	 */
	public static final boolean Debug = true;
	public static final boolean DebugCombox = true;
	public static final int RESERVE_TIMEOUT = 60000;
	public static final int ALLOC_TIMEOUT = 30000;
	public static final int CONNECTION_TIMEOUT = 30000;
	public static final String DefaultEncoding = "xdr";
	public static final String SelectedEncoding = "raw";
	public static final String DefaultProtocol = "socket";

	public static final boolean ACTIVATE_JMX = false;
	public static final boolean CONNECT_TO_POPCPP = true;
	
	/**
	 * Default constructor
	 */
	public Configuration() {
	}

}
