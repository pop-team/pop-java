package popjava.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
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
	
	/**
	 * Settable parameters for load and store options
	 */
	private enum Settable {
		SYSTEM_JOBMANAGER_CONFIG,
		DEBUG,
		DEBUG_COMBOBOX,
		RESERVE_TIMEOUT,
		ALLOC_TIMEOUT,
		CONNECTION_TIMEOUT,
		JOBMANAGER_UPDATE_INTERVAL,
		JOBMANAGER_SELF_REGISTER_INTERVAL,
		JOBMANAGER_DEFAULT_CONNECTOR,
		SEARCH_NODE_UNLOCK_TIMEOUT,
		SEARCH_NODE_SEARCH_TIMEOUT,
		SEARCH_NODE_MAX_REQUESTS,
		SEARCH_NODE_EXPLORATION_QUEUE_SIZE,
		TFC_SEARCH_TIMEOUT,
		DEFAULT_ENCODING,
		SELECTED_ENCODING,
		DEFAULT_PROTOCOL,
		ASYNC_CONSTRUCTOR,
		ACTIVATE_JMX,
		CONNECT_TO_POPCPP,
		CONNECT_TO_JAVA_JOBMANAGER,
		REDIRECT_OUTPUT_TO_ROOT,
		USE_NATIVE_SSH_IF_POSSIBLE,
		SSL_PROTOCOL_VERSION,
		SSL_KEY_STORE_FILE,
		SSL_KEY_STORE_PASSWORD,
		SSL_KEY_STORE_PRIVATE_KEY_PASSWORD,
		SSL_KEY_STORE_LOCAL_ALIAS,
		SSL_KEY_STORE_FORMAT,
		SSL_KEY_STORE_TEMP_LOCATION,
	}
	
	// Location of POPJava installation
	private static final String POPJAVA_LOCATION;
	static {
		String env = System.getenv("POPJAVA_LOCATION");
		if (Objects.isNull(env)) {
			POPJAVA_LOCATION = "./";
		} else {
			POPJAVA_LOCATION = env;
		}
	}
	
	// config files
	private static final File SYSTEM_CONFIG	     = Paths.get(POPJAVA_LOCATION, "etc", "popjava.properties").toFile();
	private static File systemJobManagerConfig   = Paths.get(POPJAVA_LOCATION, "etc", "jobmgr.conf").toFile();
	
	// properties set by the user are found here
	private static File userConfig = null;
	private static boolean usingUserConfig = false;
	private static final Properties USER_PROPERTIES = new Properties();
	private static final Properties ALL_PROPERTIES = new Properties();
	
	// user configurable attributes w/ POP's defaults
	private static boolean debug = true;
	private static boolean debugCombox = false;
	private static int reserveTimeout = 60000;
	private static int allocTimeout = 30000;
	private static int connectionTimeout = 30000;
	
	private static int jobManagerUpdateInterval = 10000;
	private static int jobManagerSelfRegisterInterval = 43_200_000;
	private static String jobManagerDefaultConnector = POPConnectorJobManager.IDENTITY;
	private static int searchNodeUnlockTimeout = 10000;
	private static int searchNodeSearchTimeout = 0;
	private static int tfcSearchTimeout = 5000;
	private static int searchNodeUnlimitedHops = Integer.MAX_VALUE;
	private static int searchNodeMaxRequests = 300;
	private static int searchNodeExplorationQueueSize = 300;
	
	private static String defaultEncoding = "xdr";
	private static String selectedEncoding = "raw";
	private static String defaultProtocol = ConnectionProtocol.SOCKET.getName();

	private static boolean asyncConstructor = true;
	private static boolean activateJmx = false;
	private static boolean connectToPOPcpp = false;
	private static boolean connectToJavaJobmanager = !connectToPOPcpp;
	
	private static boolean redirectOutputToRoot = true;
	private static boolean useNativeSSHifPossible = true;
	
	// all relevant information of the keystore (alias, keyStorePassword, privateKeyPassword, keyStoreLocation, keyStoreType, temporaryCertificatesDir)
	private static final KeyStoreOptions SSL_KEY_STORE_OPTIONS = new KeyStoreOptions();
	
	// NOTE this is waiting for TLSv1.3 to be officialized
	private static String SSLProtocolVersion = "TLSv1.2";

	/**
	 * We want a static class
	 */
	private Configuration() {
	}
	
	// initialization
	static {
		try {
			load(SYSTEM_CONFIG);
		} catch(IOException e) {
			LogWriter.writeDebugInfo("[Configuration] couldn't load '%s' using POP-Java's defaults.", SYSTEM_CONFIG);
		}
	}

	public static String getPOPJAVA_LOCATION() {
		return POPJAVA_LOCATION;
	}

	public static File getSystemJobManagerConfig() {
		return systemJobManagerConfig;
	}

	public static File getUserConfig() {
		return userConfig;
	}

	public static boolean isUsingUserConfig() {
		return usingUserConfig;
	}

	public static boolean isDebug() {
		return debug;
	}

	public static boolean isDebugCombox() {
		return debugCombox;
	}

	public static int getReserveTimeout() {
		return reserveTimeout;
	}

	public static int getAllocTimeout() {
		return allocTimeout;
	}

	public static int getConnectionTimeout() {
		return connectionTimeout;
	}

	public static int getJobManagerUpdateInterval() {
		return jobManagerUpdateInterval;
	}

	public static int getJobManagerSelfRegisterInterval() {
		return jobManagerSelfRegisterInterval;
	}

	public static String getJobManagerDefaultConnector() {
		return jobManagerDefaultConnector;
	}

	public static int getSearchNodeUnlockTimeout() {
		return searchNodeUnlockTimeout;
	}

	public static int getSearchNodeSearchTimeout() {
		return searchNodeSearchTimeout;
	}

	public static int getTFCSearchTimeout() {
		return tfcSearchTimeout;
	}

	public static int getSearchNodeUnlimitedHops() {
		return searchNodeUnlimitedHops;
	}

	public static int getSearchNodeMaxRequests() {
		return searchNodeMaxRequests;
	}

	public static int getSearchNodeExplorationQueueSize() {
		return searchNodeExplorationQueueSize;
	}

	public static String getDefaultEncoding() {
		return defaultEncoding;
	}

	public static String getSelectedEncoding() {
		return selectedEncoding;
	}

	public static String getDefaultProtocol() {
		return defaultProtocol;
	}

	public static boolean isAsyncConstructor() {
		return asyncConstructor;
	}

	public static boolean isActivateJmx() {
		return activateJmx;
	}

	public static boolean isConnectToPOPcpp() {
		return connectToPOPcpp;
	}

	public static boolean isConnectToJavaJobmanager() {
		return connectToJavaJobmanager;
	}

	public static boolean isRedirectOutputToRoot() {
		return redirectOutputToRoot;
	}

	public static boolean isUseNativeSSHifPossible() {
		return useNativeSSHifPossible;
	}

	public static KeyStoreOptions getSSLKeyStoreOptions() {
		return new KeyStoreOptions(SSL_KEY_STORE_OPTIONS);
	}

	public static String getSSLProtocolVersion() {
		return SSLProtocolVersion;
	}
	
	public static File getKeyStoreFile() {
		return new File(SSL_KEY_STORE_OPTIONS.getKeyStoreFile());
	}

	public static String getKeyStorePassword() {
		return SSL_KEY_STORE_OPTIONS.getStorePass();
	}

	public static String getKeyStorePrivateKeyPassword() {
		return SSL_KEY_STORE_OPTIONS.getKeyPass();
	}

	public static String getKeyStoreLocalAlias() {
		return SSL_KEY_STORE_OPTIONS.getAlias();
	}

	public static KeyStoreFormat getKeyStoreFormat() {
		return SSL_KEY_STORE_OPTIONS.getKeyStoreFormat();
	}

	public static File getKeyStoreTempLocation() {
		return new File(SSL_KEY_STORE_OPTIONS.getTempCertFolder());
	}



	
	public static void setSystemJobManagerConfig(File systemJobManagerConfig) {
		USER_PROPERTIES.setProperty(Settable.SYSTEM_JOBMANAGER_CONFIG.name(), systemJobManagerConfig.toString());
		Configuration.systemJobManagerConfig = systemJobManagerConfig;
	}

	public static void setUserConfig(File userConfig) {
		Configuration.userConfig = userConfig;
		Configuration.usingUserConfig = true;
	}

	public static void setDebug(boolean debug) {
		USER_PROPERTIES.setProperty(Settable.DEBUG.name(), String.valueOf(debug));
		Configuration.debug = debug;
	}

	public static void setDebugCombox(boolean debugCombox) {
		USER_PROPERTIES.setProperty(Settable.DEBUG_COMBOBOX.name(), String.valueOf(debugCombox));
		Configuration.debugCombox = debugCombox;
	}

	public static void setReserveTimeout(int reserveTimeout) {
		USER_PROPERTIES.setProperty(Settable.RESERVE_TIMEOUT.name(), String.valueOf(reserveTimeout));
		Configuration.reserveTimeout = reserveTimeout;
	}

	public static void setAllocTimeout(int allocTimeout) {
		USER_PROPERTIES.setProperty(Settable.ALLOC_TIMEOUT.name(), String.valueOf(allocTimeout));
		Configuration.allocTimeout = allocTimeout;
	}

	public static void setConnectionTimeout(int connectionTimeout) {
		USER_PROPERTIES.setProperty(Settable.CONNECTION_TIMEOUT.name(), String.valueOf(connectionTimeout));
		Configuration.connectionTimeout = connectionTimeout;
	}

	public static void setJobManagerUpdateInterval(int jobManagerUpdateInterval) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_UPDATE_INTERVAL.name(), String.valueOf(jobManagerUpdateInterval));
		Configuration.jobManagerUpdateInterval = jobManagerUpdateInterval;
	}

	public static void setJobManagerSelfRegisterInterval(int jobManagerSelfRegisterInterval) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_SELF_REGISTER_INTERVAL.name(), String.valueOf(jobManagerSelfRegisterInterval));
		Configuration.jobManagerSelfRegisterInterval = jobManagerSelfRegisterInterval;
	}

	public static void setJobManagerDefaultConnector(String jobManagerDefaultConnector) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_DEFAULT_CONNECTOR.name(), String.valueOf(jobManagerDefaultConnector));
		Configuration.jobManagerDefaultConnector = jobManagerDefaultConnector;
	}

	public static void setSearchNodeUnlockTimeout(int searchNodeUnlockTimeout) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_UNLOCK_TIMEOUT.name(), String.valueOf(searchNodeUnlockTimeout));
		Configuration.searchNodeUnlockTimeout = searchNodeUnlockTimeout;
	}

	public static void setSearchNodeSearchTimeout(int searchNodeSearchTimeout) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_SEARCH_TIMEOUT.name(), String.valueOf(searchNodeSearchTimeout));
		Configuration.searchNodeSearchTimeout = searchNodeSearchTimeout;
	}

	public static void setTFCSearchTimeout(int tfcSearchTimeout) {
		USER_PROPERTIES.setProperty(Settable.TFC_SEARCH_TIMEOUT.name(), String.valueOf(tfcSearchTimeout));
		Configuration.tfcSearchTimeout = tfcSearchTimeout;
	}

	public static void setSearchNodeMaxRequests(int searchNodeMaxRequests) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_MAX_REQUESTS.name(), String.valueOf(searchNodeMaxRequests));
		Configuration.searchNodeMaxRequests = searchNodeMaxRequests;
	}

	public static void setSearchNodeExplorationQueueSize(int searchNodeExplorationQueueSize) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_EXPLORATION_QUEUE_SIZE.name(), String.valueOf(searchNodeExplorationQueueSize));
		Configuration.searchNodeExplorationQueueSize = searchNodeExplorationQueueSize;
	}

	public static void setDefaultEncoding(String defaultEncoding) {
		USER_PROPERTIES.setProperty(Settable.DEFAULT_ENCODING.name(), defaultEncoding);
		Configuration.defaultEncoding = defaultEncoding;
	}

	public static void setSelectedEncoding(String selectedEncoding) {
		USER_PROPERTIES.setProperty(Settable.SELECTED_ENCODING.name(), selectedEncoding);
		Configuration.selectedEncoding = selectedEncoding;
	}

	public static void setDefaultProtocol(String defaultProtocol) {
		USER_PROPERTIES.setProperty(Settable.DEFAULT_PROTOCOL.name(), defaultProtocol);
		Configuration.defaultProtocol = defaultProtocol;
	}

	public static void setAsyncConstructor(boolean asyncConstructor) {
		USER_PROPERTIES.setProperty(Settable.ASYNC_CONSTRUCTOR.name(), String.valueOf(asyncConstructor));
		Configuration.asyncConstructor = asyncConstructor;
	}

	public static void setActivateJmx(boolean activateJmx) {
		USER_PROPERTIES.setProperty(Settable.ACTIVATE_JMX.name(), String.valueOf(activateJmx));
		Configuration.activateJmx = activateJmx;
	}

	public static void setConnectToPOPcpp(boolean connectToPOPcpp) {
		USER_PROPERTIES.setProperty(Settable.CONNECT_TO_POPCPP.name(), String.valueOf(connectToPOPcpp));
		Configuration.connectToPOPcpp = connectToPOPcpp;
	}

	public static void setConnectToJavaJobmanager(boolean connectToJavaJobmanager) {
		USER_PROPERTIES.setProperty(Settable.CONNECT_TO_JAVA_JOBMANAGER.name(), String.valueOf(connectToJavaJobmanager));
		Configuration.connectToJavaJobmanager = connectToJavaJobmanager;
	}

	public static void setRedirectOutputToRoot(boolean redirectOutputToRoot) {
		USER_PROPERTIES.setProperty(Settable.REDIRECT_OUTPUT_TO_ROOT.name(), String.valueOf(redirectOutputToRoot));
		Configuration.redirectOutputToRoot = redirectOutputToRoot;
	}

	public static void setUseNativeSSHifPossible(boolean useNativeSSHifPossible) {
		USER_PROPERTIES.setProperty(Settable.USE_NATIVE_SSH_IF_POSSIBLE.name(), String.valueOf(useNativeSSHifPossible));
		Configuration.useNativeSSHifPossible = useNativeSSHifPossible;
	}

	public static void setSSLProtocolVersion(String SSLProtocolVersion) {
		USER_PROPERTIES.setProperty(Settable.SSL_PROTOCOL_VERSION.name(), SSLProtocolVersion);
		Configuration.SSLProtocolVersion = SSLProtocolVersion;
	}

	public static void setKeyStoreFile(File file) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_FILE.name(), file.toString());
		Configuration.SSL_KEY_STORE_OPTIONS.setKeyStoreFile(file.toString());
	}

	public static void setKeyStorePassword(String val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_PASSWORD.name(), val);
		Configuration.SSL_KEY_STORE_OPTIONS.setStorePass(val);
	}

	public static void setKeyStorePrivateKeyPassword(String val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_PRIVATE_KEY_PASSWORD.name(), val);
		Configuration.SSL_KEY_STORE_OPTIONS.setKeyPass(val);
	}

	public static void setKeyStoreLocalAlias(String val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_LOCAL_ALIAS.name(), val);
		Configuration.SSL_KEY_STORE_OPTIONS.setAlias(val);
	}

	public static void setKeyStoreFormat(KeyStoreFormat val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_FORMAT.name(), val.name());
		Configuration.SSL_KEY_STORE_OPTIONS.setKeyStoreFormat(val);
	}

	public static void setKeyStoreTempLocation(File file) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_TEMP_LOCATION.name(), file.toString());
		Configuration.SSL_KEY_STORE_OPTIONS.setTempCertFolder(file.toString());
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
		Objects.requireNonNull(file);
		
		// mark as using user config file
		if (!file.equals(SYSTEM_CONFIG)) {
			userConfig = file.getCanonicalFile();
			usingUserConfig = true;
		}
		
		// abort if we can't load
		if (!file.exists()) {
			LogWriter.writeDebugInfo("[Configuration] '%s' doesn't exists or can't be read.", file.getCanonicalPath());
			return;
		}
		
		// merge previous values
		ALL_PROPERTIES.putAll(USER_PROPERTIES);
		
		// load user config and merge with all
		if (usingUserConfig) {
			try (InputStream in = new FileInputStream(file)) {
				USER_PROPERTIES.load(in);
			}
			// override system with user
			ALL_PROPERTIES.putAll(USER_PROPERTIES);
		}
		// load system config
		else {
			try (InputStream in = new FileInputStream(file)) {
				ALL_PROPERTIES.load(in);
			}
		}
		
		// set properties to class values
		for (Object prop : ALL_PROPERTIES.keySet()) {
			if (prop instanceof String) {
				String key = (String) prop;
				String value = ALL_PROPERTIES.getProperty(key);
				
				// get enum
				Settable keyEnum;
				try {
					keyEnum = Settable.valueOf(key.toUpperCase());
				} catch(IllegalArgumentException e) {
					LogWriter.writeDebugInfo("[Configuration] unknown key '%s'", key);
					continue;
				}
				
				try {
					switch(keyEnum) {
						case SYSTEM_JOBMANAGER_CONFIG:           systemJobManagerConfig = new File(value); break;
						case DEBUG:                              debug = Boolean.parseBoolean(value); break;
						case DEBUG_COMBOBOX:                     debugCombox = Boolean.parseBoolean(value); break;
						case RESERVE_TIMEOUT:                    reserveTimeout = Integer.parseInt(value); break;
						case ALLOC_TIMEOUT:                      allocTimeout = Integer.parseInt(value); break;
						case CONNECTION_TIMEOUT:                 connectionTimeout = Integer.parseInt(value); break;
						case JOBMANAGER_UPDATE_INTERVAL:         jobManagerUpdateInterval = Integer.parseInt(value); break;
						case JOBMANAGER_SELF_REGISTER_INTERVAL:  jobManagerSelfRegisterInterval = Integer.parseInt(value); break;
						case JOBMANAGER_DEFAULT_CONNECTOR:       jobManagerDefaultConnector = value; break;
						case SEARCH_NODE_UNLOCK_TIMEOUT:         searchNodeUnlockTimeout = Integer.parseInt(value); break;
						case SEARCH_NODE_SEARCH_TIMEOUT:         searchNodeSearchTimeout = Integer.parseInt(value); break;
						case SEARCH_NODE_MAX_REQUESTS:           searchNodeMaxRequests = Integer.parseInt(value); break;
						case SEARCH_NODE_EXPLORATION_QUEUE_SIZE: searchNodeExplorationQueueSize = Integer.parseInt(value); break;
						case TFC_SEARCH_TIMEOUT:                 tfcSearchTimeout = Integer.parseInt(value); break;
						case DEFAULT_ENCODING:                   defaultEncoding = value; break;
						case SELECTED_ENCODING:                  selectedEncoding = value; break;
						case DEFAULT_PROTOCOL:                   defaultProtocol = value; break;
						case ASYNC_CONSTRUCTOR:                  asyncConstructor = Boolean.parseBoolean(value); break;
						case ACTIVATE_JMX:                       activateJmx = Boolean.parseBoolean(value); break;
						case CONNECT_TO_POPCPP:                  connectToPOPcpp = Boolean.parseBoolean(value); break;
						case CONNECT_TO_JAVA_JOBMANAGER:         connectToJavaJobmanager = Boolean.parseBoolean(value); break;
						case REDIRECT_OUTPUT_TO_ROOT:            redirectOutputToRoot = Boolean.parseBoolean(value); break;
						case USE_NATIVE_SSH_IF_POSSIBLE:         useNativeSSHifPossible = Boolean.parseBoolean(value); break;
						case SSL_PROTOCOL_VERSION:               SSLProtocolVersion = value; break;
						case SSL_KEY_STORE_FILE:                 SSL_KEY_STORE_OPTIONS.setKeyStoreFile(value); break;
						case SSL_KEY_STORE_PASSWORD:             SSL_KEY_STORE_OPTIONS.setStorePass(value); break;
						case SSL_KEY_STORE_PRIVATE_KEY_PASSWORD: SSL_KEY_STORE_OPTIONS.setKeyPass(value); break;
						case SSL_KEY_STORE_LOCAL_ALIAS:          SSL_KEY_STORE_OPTIONS.setAlias(value); break;
						case SSL_KEY_STORE_FORMAT:               SSL_KEY_STORE_OPTIONS.setKeyStoreFormat(KeyStoreFormat.valueOf(value)); break;
						case SSL_KEY_STORE_TEMP_LOCATION:        SSL_KEY_STORE_OPTIONS.setTempCertFolder(value); break;
					}
				} catch(NumberFormatException e) {
					LogWriter.writeDebugInfo("[Configuration] unknown value '%s' for key '%s'.", value, key);
				}
			}
		}
	}
	
	/**
	 * Save the configuration to a new properties file	 
	 * 
	 * @throws IOException 
	 */
	public static void store() throws IOException {
		Objects.requireNonNull(userConfig);
		File file = userConfig;
		
		try (PrintStream out = new PrintStream(file)) {
			USER_PROPERTIES.store(out, "Automatically generated by POP-Java");
		}
	}
}
