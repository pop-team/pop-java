package popjava.util;

import popjava.util.Util.OSType;

/**
 * This class regroup some configuration values
 */
public class Configuration {

	/**
	 * Creates a new instance of POPConfiguration
	 */
	public static final boolean DEBUG = true;
	public static final boolean DEBUG_COMBOBOX = false;
	public static final int RESERVE_TIMEOUT = 60000;
	public static final int ALLOC_TIMEOUT = 30000;
	public static final int CONNECTION_TIMEOUT = 30000;
	public static final String DEFAULT_ENCODING = "xdr";
	public static final String SELECTED_ENCODING = "raw";
	public static final String DEFAULT_PROTOCOL = "socket";

	public static final boolean ACTIVATE_JMX = false;
	public static boolean CONNECT_TO_POPCPP = Util.getOSType().equals(OSType.UNIX);
	public static final boolean REDIRECT_OUTPUT_TO_ROOT = true;
	public static final boolean USE_NATIVE_SSH_IF_POSSIBLE = true;
	
	/**
	 * Default constructor
	 */
	public Configuration() {
	}

}
