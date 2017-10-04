package popjava.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import popjava.service.jobmanager.connector.POPConnectorJobManager;
import popjava.util.ssl.KeyStoreDetails;
import popjava.util.ssl.KeyStoreDetails.KeyStoreFormat;

/**
 * This class regroup some configuration values
 */
public final class Configuration {
	
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
		JOBMANAGER_PROTOCOLS,
		JOBMANAGER_PORTS,
		JOBMANAGER_EXECUTION_BASE_DIRECTORY,
		JOBMANAGER_EXECUTION_USER,
		POP_JAVA_DEAMON_PORT,
		SEARCH_NODE_UNLOCK_TIMEOUT,
		SEARCH_NODE_SEARCH_TIMEOUT,
		SEARCH_NODE_MAX_REQUESTS,
		SEARCH_NODE_EXPLORATION_QUEUE_SIZE,
		TFC_SEARCH_TIMEOUT,
		DEFAULT_ENCODING,
		SELECTED_ENCODING,
		DEFAULT_PROTOCOL,
		PROTOCOLS_WHITELIST,
		PROTOCOLS_BLACKLIST,
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
	}
	
	// instance
	private static Configuration instance;
	
	// Location of POPJava installation
	private static final String POPJAVA_LOCATION;
	static {
		String env = System.getenv("POPJAVA_LOCATION");
		if (env == null) {
			POPJAVA_LOCATION = "./";
		} else {
			POPJAVA_LOCATION = env;
		}
	}
	
	// config files
	private static final File SYSTEM_CONFIG = Paths.get(POPJAVA_LOCATION, "etc", "popjava.properties").toFile();
	private File systemJobManagerConfig = Paths.get(POPJAVA_LOCATION, "etc", "jobmgr.conf").toFile();
	
	// properties set by the user are found here
	private File userConfig = null;
	private boolean usingUserConfig = false;
	private final Properties USER_PROPERTIES = new Properties();
	private final Properties ALL_PROPERTIES = new Properties();
	
	// user configurable attributes w/ POP's defaults
	private boolean debug = false;
	private boolean debugCombox = false;
	private int reserveTimeout = 60000;
	private int allocTimeout = 30000;
	private int connectionTimeout = 30000;
	
	private int jobManagerUpdateInterval = 10000;
	private int jobManagerSelfRegisterInterval = 43_200_000;
	private String jobManagerDefaultConnector = POPConnectorJobManager.IDENTITY;
	private int searchNodeUnlockTimeout = 10000;
	private int searchNodeSearchTimeout = 0;
	private int tfcSearchTimeout = 5000;
	private int searchNodeUnlimitedHops = Integer.MAX_VALUE;
	private int searchNodeMaxRequests = 300;
	private int searchNodeExplorationQueueSize = 300;
	
	private String[] jobManagerProtocols = { "socket" };
	private int[] jobManagerPorts = { 2711 };
	private int popJavaDeamonPort = 43424;
	private String jobManagerExecutionBaseDirectory = ".";
	private String jobmanagerExecutionUser = null;
	
	private String defaultEncoding = "xdr";
	private String selectedEncoding = "raw";
	private String defaultProtocol = "socket";
	
	private Set<String> protocolsWhitelist = new HashSet<>();
	private Set<String> protocolsBlacklist = new HashSet<>();

	private boolean asyncConstructor = true;
	private boolean activateJmx = false;
	private boolean connectToPOPcpp = false;
	private boolean connectToJavaJobmanager = !connectToPOPcpp;
	
	private boolean redirectOutputToRoot = true;
	private boolean useNativeSSHifPossible = true;
	
	// all relevant information of the keystore (alias, keyStorePassword, privateKeyPassword, keyStoreLocation, keyStoreType, temporaryCertificatesDir)
	private final KeyStoreDetails SSLKeyStoreOptions = new KeyStoreDetails();
	private File SSLTemporaryCertificatesLocation = new File(".");
	
	// NOTE this is waiting for TLSv1.3 to be officialized
	private String SSLProtocolVersion = "TLSv1.2";

	/**
	 * This is a singleton
	 */
	private Configuration() {
		try {
			load(SYSTEM_CONFIG);
		} catch(IOException e) {
			System.out.format("[Configuration] couldn't load '%s' using POP-Java's defaults.\n", SYSTEM_CONFIG);
		}
	}

	public static Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
		}
		return instance;
	}

	public String getPopJavaLocation() {
		return POPJAVA_LOCATION;
	}

	public File getSystemJobManagerConfig() {
		return systemJobManagerConfig;
	}

	public File getUserConfig() {
		return userConfig;
	}

	public boolean isUsingUserConfig() {
		return usingUserConfig;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isDebugCombox() {
		return debugCombox;
	}

	public int getReserveTimeout() {
		return reserveTimeout;
	}

	public int getAllocTimeout() {
		return allocTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getJobManagerUpdateInterval() {
		return jobManagerUpdateInterval;
	}

	public int getJobManagerSelfRegisterInterval() {
		return jobManagerSelfRegisterInterval;
	}

	public String getJobManagerDefaultConnector() {
		return jobManagerDefaultConnector;
	}

	public String getJobManagerExecutionBaseDirectory() {
		return jobManagerExecutionBaseDirectory;
	}

	public String getJobmanagerExecutionUser() {
		return jobmanagerExecutionUser;
	}

	public int getSearchNodeUnlockTimeout() {
		return searchNodeUnlockTimeout;
	}

	public int getSearchNodeSearchTimeout() {
		return searchNodeSearchTimeout;
	}

	public int getTFCSearchTimeout() {
		return tfcSearchTimeout;
	}

	public int getSearchNodeUnlimitedHops() {
		return searchNodeUnlimitedHops;
	}

	public int getSearchNodeMaxRequests() {
		return searchNodeMaxRequests;
	}

	public int getSearchNodeExplorationQueueSize() {
		return searchNodeExplorationQueueSize;
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public String getSelectedEncoding() {
		return selectedEncoding;
	}

	public String getDefaultProtocol() {
		return defaultProtocol;
	}

	public boolean isAsyncConstructor() {
		return asyncConstructor;
	}

	public boolean isActivateJmx() {
		return activateJmx;
	}

	public boolean isConnectToPOPcpp() {
		return connectToPOPcpp;
	}

	public boolean isConnectToJavaJobmanager() {
		return connectToJavaJobmanager;
	}

	public boolean isRedirectOutputToRoot() {
		return redirectOutputToRoot;
	}

	public boolean isUseNativeSSHifPossible() {
		return useNativeSSHifPossible;
	}

	public KeyStoreDetails getSSLKeyStoreOptions() {
		return new KeyStoreDetails(SSLKeyStoreOptions);
	}

	public String getSSLProtocolVersion() {
		return SSLProtocolVersion;
	}
	
	public File getSSLKeyStoreFile() {
		return SSLKeyStoreOptions.getKeyStoreFile();
	}

	public String getSSLKeyStorePassword() {
		return SSLKeyStoreOptions.getKeyStorePassword();
	}

	public String getSSLKeyStorePrivateKeyPassword() {
		return SSLKeyStoreOptions.getPrivateKeyPassword();
	}

	public String getSSLKeyStoreLocalAlias() {
		return SSLKeyStoreOptions.getLocalAlias();
	}

	public KeyStoreFormat getSSLKeyStoreFormat() {
		return SSLKeyStoreOptions.getKeyStoreFormat();
	}

	public File getSSLTemporaryCertificateLocation() {
		return SSLTemporaryCertificatesLocation;
	}

	public int[] getJobManagerPorts() {
		return jobManagerPorts;
	}

	public String[] getJobManagerProtocols() {
		return jobManagerProtocols;
	}

	public int getPopJavaDeamonPort() {
		return popJavaDeamonPort;
	}

	public Set<String> getProtocolsWhitelist() {
		return Collections.unmodifiableSet(protocolsWhitelist);
	}

	public Set<String> getProtocolsBlacklist() {
		return Collections.unmodifiableSet(protocolsBlacklist);
	}


	
	
	public void setSystemJobManagerConfig(File systemJobManagerConfig) {
		USER_PROPERTIES.setProperty(Settable.SYSTEM_JOBMANAGER_CONFIG.name(), systemJobManagerConfig.toString());
		this.systemJobManagerConfig = systemJobManagerConfig;
	}

	public void setUserConfig(File userConfig) {
		this.userConfig = userConfig;
		usingUserConfig = true;
	}

	public void setDebug(boolean debug) {
		USER_PROPERTIES.setProperty(Settable.DEBUG.name(), String.valueOf(debug));
		this.debug = debug;
	}

	public void setDebugCombox(boolean debugCombox) {
		USER_PROPERTIES.setProperty(Settable.DEBUG_COMBOBOX.name(), String.valueOf(debugCombox));
		this.debugCombox = debugCombox;
	}

	public void setReserveTimeout(int reserveTimeout) {
		USER_PROPERTIES.setProperty(Settable.RESERVE_TIMEOUT.name(), String.valueOf(reserveTimeout));
		this.reserveTimeout = reserveTimeout;
	}

	public void setAllocTimeout(int allocTimeout) {
		USER_PROPERTIES.setProperty(Settable.ALLOC_TIMEOUT.name(), String.valueOf(allocTimeout));
		this.allocTimeout = allocTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		USER_PROPERTIES.setProperty(Settable.CONNECTION_TIMEOUT.name(), String.valueOf(connectionTimeout));
		this.connectionTimeout = connectionTimeout;
	}

	public void setJobManagerUpdateInterval(int jobManagerUpdateInterval) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_UPDATE_INTERVAL.name(), String.valueOf(jobManagerUpdateInterval));
		this.jobManagerUpdateInterval = jobManagerUpdateInterval;
	}

	public void setJobManagerSelfRegisterInterval(int jobManagerSelfRegisterInterval) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_SELF_REGISTER_INTERVAL.name(), String.valueOf(jobManagerSelfRegisterInterval));
		this.jobManagerSelfRegisterInterval = jobManagerSelfRegisterInterval;
	}

	public void setJobManagerDefaultConnector(String jobManagerDefaultConnector) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_DEFAULT_CONNECTOR.name(), jobManagerDefaultConnector);
		this.jobManagerDefaultConnector = jobManagerDefaultConnector;
	}

	public void setJobManagerExecutionBaseDirectory(String jobManagerExecutionBaseDirectory) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_EXECUTION_BASE_DIRECTORY.name(), jobManagerExecutionBaseDirectory);
		this.jobManagerExecutionBaseDirectory = jobManagerExecutionBaseDirectory;
	}

	public void setJobmanagerExecutionUser(String jobmanagerExecutionUser) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_EXECUTION_USER.name(), jobmanagerExecutionUser);
		this.jobmanagerExecutionUser = jobmanagerExecutionUser;
	}

	public void setSearchNodeUnlockTimeout(int searchNodeUnlockTimeout) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_UNLOCK_TIMEOUT.name(), String.valueOf(searchNodeUnlockTimeout));
		this.searchNodeUnlockTimeout = searchNodeUnlockTimeout;
	}

	public void setSearchNodeSearchTimeout(int searchNodeSearchTimeout) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_SEARCH_TIMEOUT.name(), String.valueOf(searchNodeSearchTimeout));
		this.searchNodeSearchTimeout = searchNodeSearchTimeout;
	}

	public void setTFCSearchTimeout(int tfcSearchTimeout) {
		USER_PROPERTIES.setProperty(Settable.TFC_SEARCH_TIMEOUT.name(), String.valueOf(tfcSearchTimeout));
		this.tfcSearchTimeout = tfcSearchTimeout;
	}

	public void setSearchNodeMaxRequests(int searchNodeMaxRequests) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_MAX_REQUESTS.name(), String.valueOf(searchNodeMaxRequests));
		this.searchNodeMaxRequests = searchNodeMaxRequests;
	}

	public void setSearchNodeExplorationQueueSize(int searchNodeExplorationQueueSize) {
		USER_PROPERTIES.setProperty(Settable.SEARCH_NODE_EXPLORATION_QUEUE_SIZE.name(), String.valueOf(searchNodeExplorationQueueSize));
		this.searchNodeExplorationQueueSize = searchNodeExplorationQueueSize;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		USER_PROPERTIES.setProperty(Settable.DEFAULT_ENCODING.name(), defaultEncoding);
		this.defaultEncoding = defaultEncoding;
	}

	public void setSelectedEncoding(String selectedEncoding) {
		USER_PROPERTIES.setProperty(Settable.SELECTED_ENCODING.name(), selectedEncoding);
		this.selectedEncoding = selectedEncoding;
	}

	public void setDefaultProtocol(String defaultProtocol) {
		USER_PROPERTIES.setProperty(Settable.DEFAULT_PROTOCOL.name(), defaultProtocol);
		this.defaultProtocol = defaultProtocol.toUpperCase();
	}

	public void setAsyncConstructor(boolean asyncConstructor) {
		USER_PROPERTIES.setProperty(Settable.ASYNC_CONSTRUCTOR.name(), String.valueOf(asyncConstructor));
		this.asyncConstructor = asyncConstructor;
	}

	public void setActivateJmx(boolean activateJmx) {
		USER_PROPERTIES.setProperty(Settable.ACTIVATE_JMX.name(), String.valueOf(activateJmx));
		this.activateJmx = activateJmx;
	}

	public void setConnectToPOPcpp(boolean connectToPOPcpp) {
		USER_PROPERTIES.setProperty(Settable.CONNECT_TO_POPCPP.name(), String.valueOf(connectToPOPcpp));
		this.connectToPOPcpp = connectToPOPcpp;
	}

	public void setConnectToJavaJobmanager(boolean connectToJavaJobmanager) {
		USER_PROPERTIES.setProperty(Settable.CONNECT_TO_JAVA_JOBMANAGER.name(), String.valueOf(connectToJavaJobmanager));
		this.connectToJavaJobmanager = connectToJavaJobmanager;
	}

	public void setRedirectOutputToRoot(boolean redirectOutputToRoot) {
		USER_PROPERTIES.setProperty(Settable.REDIRECT_OUTPUT_TO_ROOT.name(), String.valueOf(redirectOutputToRoot));
		this.redirectOutputToRoot = redirectOutputToRoot;
	}

	public void setUseNativeSSHifPossible(boolean useNativeSSHifPossible) {
		USER_PROPERTIES.setProperty(Settable.USE_NATIVE_SSH_IF_POSSIBLE.name(), String.valueOf(useNativeSSHifPossible));
		this.useNativeSSHifPossible = useNativeSSHifPossible;
	}

	public void setSSLProtocolVersion(String SSLProtocolVersion) {
		USER_PROPERTIES.setProperty(Settable.SSL_PROTOCOL_VERSION.name(), SSLProtocolVersion);
		this.SSLProtocolVersion = SSLProtocolVersion;
	}

	public void setSSLKeyStoreFile(File file) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_FILE.name(), file.toString());
		SSLKeyStoreOptions.setKeyStoreFile(file);
	}

	public void setSSLKeyStorePassword(String val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_PASSWORD.name(), val);
		SSLKeyStoreOptions.setKeyStorePassword(val);
	}

	public void setSSLKeyStorePrivateKeyPassword(String val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_PRIVATE_KEY_PASSWORD.name(), val);
		SSLKeyStoreOptions.setPrivateKeyPassword(val);
	}

	public void setSSLKeyStoreLocalAlias(String val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_LOCAL_ALIAS.name(), val);
		SSLKeyStoreOptions.setLocalAlias(val);
	}

	public void setSSLKeyStoreFormat(KeyStoreFormat val) {
		USER_PROPERTIES.setProperty(Settable.SSL_KEY_STORE_FORMAT.name(), val.name());
		SSLKeyStoreOptions.setKeyStoreFormat(val);
	}

	public void setSSLTemporaryCertificateDirectory(File file) {
		SSLTemporaryCertificatesLocation = file;
	}
	
	public void setSSLKeyStoreOptions(KeyStoreDetails options) {
		setSSLKeyStoreFile(options.getKeyStoreFile());
		setSSLKeyStoreFormat(options.getKeyStoreFormat());
		setSSLKeyStoreLocalAlias(options.getLocalAlias());
		setSSLKeyStorePassword(options.getKeyStorePassword());
		setSSLKeyStorePrivateKeyPassword(options.getPrivateKeyPassword());
	}

	public void setJobManagerPorts(int[] jobManagerPorts) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_PORTS.name(), Arrays.toString(jobManagerPorts));
		this.jobManagerPorts = jobManagerPorts;
	}

	public void setJobManagerProtocols(String[] jobManagerProtocols) {
		USER_PROPERTIES.setProperty(Settable.JOBMANAGER_PROTOCOLS.name(), Arrays.toString(jobManagerProtocols));
		this.jobManagerProtocols = jobManagerProtocols;
	}

	public void setPopJavaDeamonPort(int popJavaDeamonPort) {
		USER_PROPERTIES.setProperty(Settable.POP_JAVA_DEAMON_PORT.name(), String.valueOf(popJavaDeamonPort));
		this.popJavaDeamonPort = popJavaDeamonPort;
	}

	public void setProtocolsWhitelist(Set<String> protocolsWhitelist) {
		USER_PROPERTIES.setProperty(Settable.PROTOCOLS_WHITELIST.name(), protocolsWhitelist.toString());
		this.protocolsWhitelist = new HashSet<>(protocolsWhitelist);
	}

	public void setProtocolsBlacklist(Set<String> protocolsBlacklist) {
		USER_PROPERTIES.setProperty(Settable.PROTOCOLS_BLACKLIST.name(), protocolsBlacklist.toString());
		this.protocolsBlacklist = new HashSet<>(protocolsBlacklist);
	}
	
	
	/**
	 * Load a custom configuration file on top of the system and defaults one.
	 * Hierarchy:
	 *       User
	 *      Machine
	 *   POP Defaults
	 * 
	 * @param file The properties file to load
	 * @throws java.io.IOException 
	 */
	public void load(File file) throws IOException {
		long start = System.currentTimeMillis();
		Objects.requireNonNull(file);
		
		// mark as using user config file
		if (!file.equals(SYSTEM_CONFIG)) {
			userConfig = file.getCanonicalFile();
			usingUserConfig = true;
		}
		
		// abort if we can't load
		if (!file.exists()) {
			if (debug) {
				System.out.format("[Configuration] '%s' doesn't exists or can't be read.\n", file.getCanonicalPath());
			}
			return;
		}
		
		// merge manually set values
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
					if (debug) {
						System.out.format("[Configuration] unknown key '%s'\n", key);
					}
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
						case JOBMANAGER_EXECUTION_BASE_DIRECTORY:jobManagerExecutionBaseDirectory = value; break;
						case JOBMANAGER_EXECUTION_USER:          jobmanagerExecutionUser = value; break;
						case JOBMANAGER_PORTS:
							String[] ports = matchRegEx(value, "\\d+");
							jobManagerPorts = new int[ports.length];
							for (int i = 0; i < ports.length; i++) {
								jobManagerPorts[i] = Integer.parseInt(ports[i]);
							}
							break;
						case JOBMANAGER_PROTOCOLS:               jobManagerProtocols = matchRegEx(value, "[\\w\\d]+"); break;
						case POP_JAVA_DEAMON_PORT:               popJavaDeamonPort = Integer.parseInt(value); break;
						case SEARCH_NODE_UNLOCK_TIMEOUT:         searchNodeUnlockTimeout = Integer.parseInt(value); break;
						case SEARCH_NODE_SEARCH_TIMEOUT:         searchNodeSearchTimeout = Integer.parseInt(value); break;
						case SEARCH_NODE_MAX_REQUESTS:           searchNodeMaxRequests = Integer.parseInt(value); break;
						case SEARCH_NODE_EXPLORATION_QUEUE_SIZE: searchNodeExplorationQueueSize = Integer.parseInt(value); break;
						case TFC_SEARCH_TIMEOUT:                 tfcSearchTimeout = Integer.parseInt(value); break;
						case DEFAULT_ENCODING:                   defaultEncoding = value; break;
						case SELECTED_ENCODING:                  selectedEncoding = value; break;
						case DEFAULT_PROTOCOL:                   defaultProtocol = value.toUpperCase(); break;
						case PROTOCOLS_WHITELIST:
							protocolsWhitelist.clear();
							protocolsWhitelist.addAll(Arrays.asList(matchRegEx(value, "[\\w\\d]+")));
						case PROTOCOLS_BLACKLIST:
							protocolsBlacklist.clear();
							protocolsBlacklist.addAll(Arrays.asList(matchRegEx(value, "[\\w\\d]+")));
						case ASYNC_CONSTRUCTOR:                  asyncConstructor = Boolean.parseBoolean(value); break;
						case ACTIVATE_JMX:                       activateJmx = Boolean.parseBoolean(value); break;
						case CONNECT_TO_POPCPP:                  connectToPOPcpp = Boolean.parseBoolean(value); break;
						case CONNECT_TO_JAVA_JOBMANAGER:         connectToJavaJobmanager = Boolean.parseBoolean(value); break;
						case REDIRECT_OUTPUT_TO_ROOT:            redirectOutputToRoot = Boolean.parseBoolean(value); break;
						case USE_NATIVE_SSH_IF_POSSIBLE:         useNativeSSHifPossible = Boolean.parseBoolean(value); break;
						case SSL_PROTOCOL_VERSION:               SSLProtocolVersion = value; break;
						case SSL_KEY_STORE_FILE:                 SSLKeyStoreOptions.setKeyStoreFile(new File(value)); break;
						case SSL_KEY_STORE_PASSWORD:             SSLKeyStoreOptions.setKeyStorePassword(value); break;
						case SSL_KEY_STORE_PRIVATE_KEY_PASSWORD: SSLKeyStoreOptions.setPrivateKeyPassword(value); break;
						case SSL_KEY_STORE_LOCAL_ALIAS:          SSLKeyStoreOptions.setLocalAlias(value); break;
						case SSL_KEY_STORE_FORMAT:               SSLKeyStoreOptions.setKeyStoreFormat(KeyStoreFormat.valueOf(value)); break;
					}
				} catch(NumberFormatException e) {
					if (debug) {
						System.out.format("[Configuration] unknown value '%s' for key '%s'.\n", value, key);
					}
				}
			}
		}
		if (debug) {
			long end = System.currentTimeMillis();
			System.out.format("[Configuration] loaded '%s' in %d ms\n", file.getCanonicalPath(), end - start);
		}
	}
	
	/**
	 * Save the configuration to a new properties file	 
	 * 
	 * @throws IOException 
	 */
	public void store() throws IOException {
		Objects.requireNonNull(userConfig, "A user configuration file must be provided via setUserConfig or load.");
		File file = userConfig;
		
		try (PrintStream out = new PrintStream(file)) {
			USER_PROPERTIES.store(out, "Automatically generated by POP-Java");
		}
	}
	
	private String[] matchRegEx(String value, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(value);
		List<String> matches = new ArrayList<>();
		while (m.find()) {
			matches.add(m.group());
		}
		return matches.toArray(new String[matches.size()]);
	}
}
