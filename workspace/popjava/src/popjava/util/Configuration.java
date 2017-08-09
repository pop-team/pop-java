package popjava.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import popjava.baseobject.ConnectionProtocol;
import popjava.service.jobmanager.connector.POPConnectorJobManager;
import popjava.util.ssl.KeyStoreOptions;
import popjava.util.ssl.KeyStoreOptions.KeyStoreFormat;

/**
 * This class regroup some configuration values
 */
public class Configuration {
	
	private static String POPJAVA_CONFIG = System.getenv("POPJAVA_LOCATION") + "/etc/popjava.conf";
	public static String DEFAULT_JM_CONFIG_FILE = System.getenv("POPJAVA_LOCATION") + "/etc/jobmgr.conf";

	/**
	 * Creates a new instance of POPConfiguration
	 */
	public static boolean DEBUG = true;
	public static boolean DEBUG_COMBOBOX = false;
	public static int RESERVE_TIMEOUT = 60000;
	public static int ALLOC_TIMEOUT = 30000;
	public static int CONNECTION_TIMEOUT = 30000;
	
	public static int UPDATE_MIN_INTERVAL = 10000;
	public static int SELF_REGISTER_INTERVAL = 43200_000;
	public static int UNLOCK_TIMEOUT = 10000;
	public static int SEARCH_TIMEOUT = 0;
	public static int TFC_SEARCH_TIMEOUT = 5000;
	public static int UNLIMITED_HOPS = Integer.MAX_VALUE;
	public static int MAXREQTOSAVE = 300;
	public static final String DEFAULT_CONNECTOR = POPConnectorJobManager.IDENTITY;
	public static int EXPLORATION_MAX_SIZE = 300;
	
	public static String DEFAULT_ENCODING = "xdr";
	public static String SELECTED_ENCODING = "raw";
	public static String DEFAULT_PROTOCOL = ConnectionProtocol.SSL.getName();

	public static boolean ASYNC_CONSTRUCTOR = true;
	public static boolean ACTIVATE_JMX = false;
	public static boolean CONNECT_TO_POPCPP = false;//Util.getOSType().equals(OSType.UNIX);
	public static boolean CONNECT_TO_JAVA_JOBMANAGER = !CONNECT_TO_POPCPP;
	
	public static boolean REDIRECT_OUTPUT_TO_ROOT = true;
	public static boolean USE_NATIVE_SSH_IF_POSSIBLE = true;
	
	// all relevant information of the keystore (alias, keyStorePassword, privateKeyPassword, keyStoreLocation, keyStoreType, temporaryCertificatesDir)
	public static KeyStoreOptions SSL_KEY_STORE_OPTIONS = new KeyStoreOptions("poplocal", "poplocalstore", "poplocalkey", "poplocal.jks", KeyStoreFormat.JKS, "tempCertifiates");
	
	// NOTE this is waiting for TLSv1.3 to be officialized
	public static String SSL_PROTOCOL_VERSION = "TLSv1.2";

	/**
	 * Default constructor
	 */
	private Configuration() {
	}
	
	// initialization
	static {
		try {
			load(new File(POPJAVA_CONFIG));
		} catch(IOException e) {
			LogWriter.writeDebugInfo("[Configuration] couldn't load %s: %s", POPJAVA_CONFIG, e.getMessage());
		}
	}

	/**
	 * Return the location of the current configuration file.
	 * If no user one has been set it will be the system one.
	 * 
	 * @return 
	 */
	public static String getPOPJavaConfig() {
		return POPJAVA_CONFIG;
	}
	
	/**
	 * Load a custom configuration file on top of the system and defaults one.
	 * Hierarchy:
	 *       User
	 *      Machine
	 *   POP Defaults
	 * 
	 * @param file The file 
	 * @throws java.io.IOException 
	 */
	public static void load(File file) throws IOException {
		Objects.nonNull(file);
		
		POPJAVA_CONFIG = file.getAbsolutePath();
		
		if (!file.exists()) {
			return;
		}
		
		// TODO load POP defaults
		Properties props = new Properties();
		props.load(new FileInputStream(file));
		
		// set properties to class values
		for (Object prop : props.keySet()) {
			if (prop instanceof String) {
				String key = (String) prop;
				String value = props.getProperty(key);
				
				switch(key.trim().toUpperCase()) {
					case "SSL_KEY_STORE_FILE": SSL_KEY_STORE_OPTIONS.setKeyStoreFile(value); break;
					case "SSL_KEY_STORE_PASSWORD": SSL_KEY_STORE_OPTIONS.setStorePass(value); break;
					case "SSL_KEY_STORE_PRIVATE_KEY_PASSWORD": SSL_KEY_STORE_OPTIONS.setKeyPass(value); break;
					case "SSL_KEY_STORE_LOCAL_ALIAS": SSL_KEY_STORE_OPTIONS.setAlias(value); break;
					case "SSL_KEY_STORE_TYPE": SSL_KEY_STORE_OPTIONS.setKeyStoreFormat(KeyStoreFormat.valueOf(value)); break;
					case "SSL_KEY_STORE_TEMP_LOCATION": SSL_KEY_STORE_OPTIONS.setTempCertFolder(value); break;
				}
			}
		}
	}
	
	public static void save() {
		
	}
}
