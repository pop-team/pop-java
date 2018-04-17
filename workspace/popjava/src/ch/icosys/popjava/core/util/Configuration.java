package ch.icosys.popjava.core.util;

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

import ch.icosys.popjava.core.util.ssl.KeyStoreDetails;
import ch.icosys.popjava.core.util.ssl.KeyStoreDetails.KeyStoreFormat;

/**
 * This class regroup some configuration values
 */
public final class Configuration {

	/**
	 * Settable parameters for load and store options
	 */
	private enum Settable {
		SYSTEM_JOBMANAGER_CONFIG, DEBUG, DEBUG_COMBOX, RESERVE_TIMEOUT, ALLOC_TIMEOUT, CONNECTION_TIMEOUT, JOBMANAGER_UPDATE_INTERVAL, JOBMANAGER_SELF_REGISTER_INTERVAL, JOBMANAGER_DEFAULT_CONNECTOR, JOBMANAGER_PROTOCOLS, JOBMANAGER_PORTS, JOBMANAGER_EXECUTION_BASE_DIRECTORY, JOBMANAGER_EXECUTION_USER, POP_JAVA_DEAMON_PORT, SEARCH_NODE_UNLOCK_TIMEOUT, SEARCH_NODE_SEARCH_TIMEOUT, SEARCH_NODE_MAX_REQUESTS, SEARCH_NODE_EXPLORATION_QUEUE_SIZE, TFC_SEARCH_TIMEOUT, DEFAULT_ENCODING, SELECTED_ENCODING, DEFAULT_PROTOCOL, DEFAULT_NETWORK, ALLOCATE_PORT_RANGE, PROTOCOLS_WHITELIST, PROTOCOLS_BLACKLIST, ASYNC_CONSTRUCTOR, ACTIVATE_JMX, CONNECT_TO_POPCPP, CONNECT_TO_JAVA_JOBMANAGER, REDIRECT_OUTPUT_TO_ROOT, USE_NATIVE_SSH_IF_POSSIBLE, SSL_PROTOCOL_VERSION, SSL_KEY_STORE_FILE, SSL_KEY_STORE_PASSWORD, SSL_KEY_STORE_PRIVATE_KEY_PASSWORD, SSL_KEY_STORE_FORMAT,
	}

	// instance
	private static Configuration instance;

	// Location of POPJava installation
	private static final String POPJAVA_LOCATION;

	private static final Boolean ENV_DEBUG;
	static {
		String location = System.getenv("POPJAVA_LOCATION");
		String debug = System.getenv("POP_DEBUG");
		if (location == null) {
			POPJAVA_LOCATION = new File("./").getAbsolutePath();
		} else {
			POPJAVA_LOCATION = new File(location).getAbsolutePath();
		}
		if (debug != null) {
			ENV_DEBUG = Boolean.getBoolean(debug);
		} else {
			ENV_DEBUG = null;
		}
	}

	// config files
	private static final File SYSTEM_CONFIG = Paths.get(POPJAVA_LOCATION, "etc", "popjava.properties").toFile();

	private File systemJobManagerConfig = Paths.get(POPJAVA_LOCATION, "etc", "jobmgr.yml").toFile();

	// properties set by the user are found here
	private File userConfig = null;

	private boolean usingUserConfig = false;

	private final Properties USER_PROPERTIES = new Properties();

	private final Properties ALL_PROPERTIES = new Properties();

	// user configurable attributes w/ POP's defaults
	private boolean debug = ENV_DEBUG != null ? ENV_DEBUG : false;

	private boolean debugCombox = false;

	private int reserveTimeout = 60000;

	private int allocTimeout = 30000;

	private int connectionTimeout = 30000;

	private int jobManagerUpdateInterval = 10000;

	private int jobManagerSelfRegisterInterval = 43_200_000;

	private String jobManagerDefaultConnector = "jobmanager";

	private int searchNodeUnlockTimeout = 10000;

	private int searchNodeSearchTimeout = 0;

	private int tfcSearchTimeout = 5000;

	private int searchNodeUnlimitedHops = Integer.MAX_VALUE;

	private int searchNodeMaxRequests = 300;

	private int searchNodeExplorationQueueSize = 300;

	private String[] jobManagerProtocols = { "socket" };

	private int[] jobManagerPorts = { 2711 };

	private int popJavaDaemonPort = 43424;

	private String jobManagerExecutionBaseDirectory = ".";

	private String jobmanagerExecutionUser = null;

	private String defaultEncoding = "xdr";

	private String selectedEncoding = "raw";

	private String defaultProtocol = "socket";

	private String defaultNetwork = "";

	private int allocatePortRange = 49152;

	private final Set<String> protocolsWhitelist = new HashSet<>();

	private final Set<String> protocolsBlacklist = new HashSet<>();

	private boolean asyncConstructor = true;

	private boolean activateJmx = false;

	private boolean connectToPOPcpp = false;

	private boolean connectToJavaJobmanager = !connectToPOPcpp;

	private boolean redirectOutputToRoot = true;

	private boolean useNativeSSHifPossible = true;

	// all relevant information of the keystore (alias, keyStorePassword,
	// privateKeyPassword, keyStoreLocation, keyStoreType,
	// temporaryCertificatesDir)
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
		} catch (IOException e) {
			System.out.format("[Configuration] couldn't load '%s' using POP-Java's defaults.\n", SYSTEM_CONFIG);
		}
	}

	public static Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
		}
		return instance;
	}

	/**
	 * @return The location of the PopJava installation directory
	 */
	public String getPopJavaLocation() {
		return POPJAVA_LOCATION;
	}

	/**
	 * @return The location of the Job Manager configuration file
	 */
	public File getSystemJobManagerConfig() {
		return systemJobManagerConfig;
	}

	/**
	 * @return The location of the user configuration file, if given
	 */
	public File getUserConfig() {
		return userConfig;
	}

	/**
	 * @return true is a user configuration file is set
	 */
	public boolean isUsingUserConfig() {
		return usingUserConfig;
	}

	/**
	 * @return true if debug information will be displayed
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @return true if the combox connection will print their debug information
	 */
	public boolean isDebugCombox() {
		return debugCombox;
	}

	/**
	 * @return how many ms after a job manager will drop a request
	 */
	public int getReserveTimeout() {
		return reserveTimeout;
	}

	/**
	 * @return how many ms a Interface should wait for a Broker to contact it
	 */
	public int getAllocTimeout() {
		return allocTimeout;
	}

	/**
	 * @return how many ms until a lost connection is dropped
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * @return interval in ms for the job manager to refresh itself
	 */
	public int getJobManagerUpdateInterval() {
		return jobManagerUpdateInterval;
	}

	/**
	 * @return interval in ms for the job manager to signal its present to its
	 *         neighbors
	 */
	@Deprecated
	public int getJobManagerSelfRegisterInterval() {
		return jobManagerSelfRegisterInterval;
	}

	/**
	 * @return the default approach a job manager will use in case none was
	 *         expressed in the OD
	 */
	public String getJobManagerDefaultConnector() {
		return jobManagerDefaultConnector;
	}

	/**
	 * @return where the job manager should execute the object it will create
	 */
	public String getJobManagerExecutionBaseDirectory() {
		return jobManagerExecutionBaseDirectory;
	}

	/**
	 * @return the use which should be used to execute the object
	 */
	public String getJobmanagerExecutionUser() {
		return jobmanagerExecutionUser;
	}

	/**
	 * @return default timeout when looking for a single answer
	 */
	public int getSearchNodeUnlockTimeout() {
		return searchNodeUnlockTimeout;
	}

	/**
	 * @return default timeout when looking for multiple answers
	 */
	public int getSearchNodeSearchTimeout() {
		return searchNodeSearchTimeout;
	}

	/**
	 * @return default time a TFC research should last
	 */
	public int getTFCSearchTimeout() {
		return tfcSearchTimeout;
	}

	/**
	 * @return how many nodes should we explore
	 */
	public int getSearchNodeUnlimitedHops() {
		return searchNodeUnlimitedHops;
	}

	/**
	 * @return how many requests should be remember at the same time
	 */
	public int getSearchNodeMaxRequests() {
		return searchNodeMaxRequests;
	}

	/**
	 * @return how many visited nodes should be remember when searching
	 */
	public int getSearchNodeExplorationQueueSize() {
		return searchNodeExplorationQueueSize;
	}

	/**
	 * @return
	 */
	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	/**
	 * @return
	 */
	public String getSelectedEncoding() {
		return selectedEncoding;
	}

	/**
	 * @return default communication protocol
	 */
	public String getDefaultProtocol() {
		return defaultProtocol;
	}

	/**
	 * @return default POP network signaled
	 */
	public String getDefaultNetwork() {
		return defaultNetwork;
	}

	/**
	 * @return from where do we start allocating ports
	 */
	public int getAllocatePortRange() {
		return allocatePortRange;
	}

	/**
	 * @return is the constructor async
	 */
	public boolean isAsyncConstructor() {
		return asyncConstructor;
	}

	/**
	 * @return is jmx active for remote analysis
	 */
	public boolean isActivateJmx() {
		return activateJmx;
	}

	/**
	 * @return use the POPC job manager
	 */
	public boolean isConnectToPOPcpp() {
		return connectToPOPcpp;
	}

	/**
	 * @return use the Java job manager
	 */
	public boolean isConnectToJavaJobmanager() {
		return connectToJavaJobmanager;
	}

	/**
	 * @return print ssh output locally
	 */
	public boolean isRedirectOutputToRoot() {
		return redirectOutputToRoot;
	}

	/**
	 * @return use the available ssh command if possible
	 */
	public boolean isUseNativeSSHifPossible() {
		return useNativeSSHifPossible;
	}

	/**
	 * @return information on the keystore containing the private keys
	 */
	public KeyStoreDetails getSSLKeyStoreOptions() {
		return new KeyStoreDetails(SSLKeyStoreOptions);
	}

	/**
	 * @return which TLS version are we using
	 */
	public String getSSLProtocolVersion() {
		return SSLProtocolVersion;
	}

	/**
	 * @return alias for {@link KeyStoreDetails#getKeyStoreFile()}
	 */
	public File getSSLKeyStoreFile() {
		return SSLKeyStoreOptions.getKeyStoreFile();
	}

	/**
	 * @return alias for {@link KeyStoreDetails#getKeyStorePassword()}
	 */
	public String getSSLKeyStorePassword() {
		return SSLKeyStoreOptions.getKeyStorePassword();
	}

	/**
	 * @return alias for {@link KeyStoreDetails#getPrivateKeyPassword()}
	 */
	public String getSSLKeyStorePrivateKeyPassword() {
		return SSLKeyStoreOptions.getPrivateKeyPassword();
	}

	/**
	 * @return alias for {@link KeyStoreDetails#getKeyStoreFormat()}
	 */
	public KeyStoreFormat getSSLKeyStoreFormat() {
		return SSLKeyStoreOptions.getKeyStoreFormat();
	}

	/**
	 * @return the location for this object temporary certificates
	 */
	public File getSSLTemporaryCertificateLocation() {
		return SSLTemporaryCertificatesLocation;
	}

	/**
	 * @return the ports to be used by the job manager
	 */
	public int[] getJobManagerPorts() {
		return Arrays.copyOf(jobManagerPorts, jobManagerPorts.length);
	}

	/**
	 * @return the protocols to the used by the job manger
	 */
	public String[] getJobManagerProtocols() {
		return Arrays.copyOf(jobManagerProtocols, jobManagerProtocols.length);
	}

	/**
	 * @return the port used by the object creation daemon
	 */
	public int getPopJavaDaemonPort() {
		return popJavaDaemonPort;
	}

	/**
	 * @return the only protocol we can use, unless in blacklist
	 */
	public Set<String> getProtocolsWhitelist() {
		return Collections.unmodifiableSet(protocolsWhitelist);
	}

	/**
	 * @return the protocols we can't use
	 */
	public Set<String> getProtocolsBlacklist() {
		return Collections.unmodifiableSet(protocolsBlacklist);
	}

	public void setSystemJobManagerConfig(File systemJobManagerConfig) {
		setUserProp(Settable.SYSTEM_JOBMANAGER_CONFIG, systemJobManagerConfig);
		this.systemJobManagerConfig = systemJobManagerConfig;
	}

	public void setUserConfig(File userConfig) {
		this.userConfig = userConfig;
		usingUserConfig = userConfig != null;
	}

	public void setDebug(boolean debug) {
		if (ENV_DEBUG == null) {
			setUserProp(Settable.DEBUG, debug);
			this.debug = debug;
		}
	}

	public void setDebugCombox(boolean debugCombox) {
		setUserProp(Settable.DEBUG_COMBOX, debugCombox);
		this.debugCombox = debugCombox;
	}

	public void setReserveTimeout(int reserveTimeout) {
		setUserProp(Settable.RESERVE_TIMEOUT, reserveTimeout);
		this.reserveTimeout = reserveTimeout;
	}

	public void setAllocTimeout(int allocTimeout) {
		setUserProp(Settable.ALLOC_TIMEOUT, allocTimeout);
		this.allocTimeout = allocTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		setUserProp(Settable.CONNECTION_TIMEOUT, connectionTimeout);
		this.connectionTimeout = connectionTimeout;
	}

	public void setJobManagerUpdateInterval(int jobManagerUpdateInterval) {
		setUserProp(Settable.JOBMANAGER_UPDATE_INTERVAL, jobManagerUpdateInterval);
		this.jobManagerUpdateInterval = jobManagerUpdateInterval;
	}

	public void setJobManagerSelfRegisterInterval(int jobManagerSelfRegisterInterval) {
		setUserProp(Settable.JOBMANAGER_SELF_REGISTER_INTERVAL, jobManagerSelfRegisterInterval);
		this.jobManagerSelfRegisterInterval = jobManagerSelfRegisterInterval;
	}

	public void setJobManagerDefaultConnector(String jobManagerDefaultConnector) {
		setUserProp(Settable.JOBMANAGER_DEFAULT_CONNECTOR, jobManagerDefaultConnector);
		this.jobManagerDefaultConnector = jobManagerDefaultConnector;
	}

	public void setJobManagerExecutionBaseDirectory(String jobManagerExecutionBaseDirectory) {
		setUserProp(Settable.JOBMANAGER_EXECUTION_BASE_DIRECTORY, jobManagerExecutionBaseDirectory);
		this.jobManagerExecutionBaseDirectory = jobManagerExecutionBaseDirectory;
	}

	public void setJobmanagerExecutionUser(String jobmanagerExecutionUser) {
		setUserProp(Settable.JOBMANAGER_EXECUTION_USER, jobmanagerExecutionUser);
		this.jobmanagerExecutionUser = jobmanagerExecutionUser;
	}

	public void setSearchNodeUnlockTimeout(int searchNodeUnlockTimeout) {
		setUserProp(Settable.SEARCH_NODE_UNLOCK_TIMEOUT, searchNodeUnlockTimeout);
		this.searchNodeUnlockTimeout = searchNodeUnlockTimeout;
	}

	public void setSearchNodeSearchTimeout(int searchNodeSearchTimeout) {
		setUserProp(Settable.SEARCH_NODE_SEARCH_TIMEOUT, searchNodeSearchTimeout);
		this.searchNodeSearchTimeout = searchNodeSearchTimeout;
	}

	public void setTFCSearchTimeout(int tfcSearchTimeout) {
		setUserProp(Settable.TFC_SEARCH_TIMEOUT, tfcSearchTimeout);
		this.tfcSearchTimeout = tfcSearchTimeout;
	}

	public void setSearchNodeMaxRequests(int searchNodeMaxRequests) {
		setUserProp(Settable.SEARCH_NODE_MAX_REQUESTS, searchNodeMaxRequests);
		this.searchNodeMaxRequests = searchNodeMaxRequests;
	}

	public void setSearchNodeExplorationQueueSize(int searchNodeExplorationQueueSize) {
		setUserProp(Settable.SEARCH_NODE_EXPLORATION_QUEUE_SIZE, searchNodeExplorationQueueSize);
		this.searchNodeExplorationQueueSize = searchNodeExplorationQueueSize;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		setUserProp(Settable.DEFAULT_ENCODING, defaultEncoding);
		this.defaultEncoding = defaultEncoding;
	}

	public void setSelectedEncoding(String selectedEncoding) {
		setUserProp(Settable.SELECTED_ENCODING, selectedEncoding);
		this.selectedEncoding = selectedEncoding;
	}

	public void setDefaultProtocol(String defaultProtocol) {
		setUserProp(Settable.DEFAULT_PROTOCOL, defaultProtocol);
		this.defaultProtocol = defaultProtocol.toUpperCase();
	}

	public void setAllocatePortRange(int allocatePortRange) {
		setUserProp(Settable.ALLOCATE_PORT_RANGE, allocatePortRange);
		this.allocatePortRange = allocatePortRange;
	}

	public void setDefaultNetwork(String systemDefaultNetwork) {
		setUserProp(Settable.DEFAULT_NETWORK, systemDefaultNetwork);
		this.defaultNetwork = systemDefaultNetwork;
	}

	public void setAsyncConstructor(boolean asyncConstructor) {
		setUserProp(Settable.ASYNC_CONSTRUCTOR, asyncConstructor);
		this.asyncConstructor = asyncConstructor;
	}

	public void setActivateJmx(boolean activateJmx) {
		setUserProp(Settable.ACTIVATE_JMX, activateJmx);
		this.activateJmx = activateJmx;
	}

	public void setConnectToPOPcpp(boolean connectToPOPcpp) {
		setUserProp(Settable.CONNECT_TO_POPCPP, connectToPOPcpp);
		this.connectToPOPcpp = connectToPOPcpp;
	}

	public void setConnectToJavaJobmanager(boolean connectToJavaJobmanager) {
		setUserProp(Settable.CONNECT_TO_JAVA_JOBMANAGER, connectToJavaJobmanager);
		this.connectToJavaJobmanager = connectToJavaJobmanager;
	}

	public void setRedirectOutputToRoot(boolean redirectOutputToRoot) {
		setUserProp(Settable.REDIRECT_OUTPUT_TO_ROOT, redirectOutputToRoot);
		this.redirectOutputToRoot = redirectOutputToRoot;
	}

	public void setUseNativeSSHifPossible(boolean useNativeSSHifPossible) {
		setUserProp(Settable.USE_NATIVE_SSH_IF_POSSIBLE, useNativeSSHifPossible);
		this.useNativeSSHifPossible = useNativeSSHifPossible;
	}

	public void setSSLProtocolVersion(String SSLProtocolVersion) {
		setUserProp(Settable.SSL_PROTOCOL_VERSION, SSLProtocolVersion);
		this.SSLProtocolVersion = SSLProtocolVersion;
	}

	public void setSSLKeyStoreFile(File file) {
		setUserProp(Settable.SSL_KEY_STORE_FILE, file);
		SSLKeyStoreOptions.setKeyStoreFile(file);
	}

	public void setSSLKeyStorePassword(String val) {
		setUserProp(Settable.SSL_KEY_STORE_PASSWORD, val);
		SSLKeyStoreOptions.setKeyStorePassword(val);
	}

	public void setSSLKeyStorePrivateKeyPassword(String val) {
		setUserProp(Settable.SSL_KEY_STORE_PRIVATE_KEY_PASSWORD, val);
		SSLKeyStoreOptions.setPrivateKeyPassword(val);
	}

	public void setSSLKeyStoreFormat(KeyStoreFormat val) {
		setUserProp(Settable.SSL_KEY_STORE_FORMAT, val);
		SSLKeyStoreOptions.setKeyStoreFormat(val);
	}

	public void setSSLTemporaryCertificateDirectory(File file) {
		SSLTemporaryCertificatesLocation = file;
	}

	public void setSSLKeyStoreOptions(KeyStoreDetails options) {
		if (options == null) {
			setSSLKeyStoreFile(null);
			setSSLKeyStoreFormat(null);
			setSSLKeyStorePassword(null);
			setSSLKeyStorePrivateKeyPassword(null);
		} else {
			setSSLKeyStoreFile(options.getKeyStoreFile());
			setSSLKeyStoreFormat(options.getKeyStoreFormat());
			setSSLKeyStorePassword(options.getKeyStorePassword());
			setSSLKeyStorePrivateKeyPassword(options.getPrivateKeyPassword());
		}
	}

	public void setJobManagerPorts(int[] jobManagerPorts) {
		setUserProp(Settable.JOBMANAGER_PORTS, Arrays.toString(jobManagerPorts));
		this.jobManagerPorts = Arrays.copyOf(jobManagerPorts, jobManagerPorts.length);
	}

	public void setJobManagerProtocols(String[] jobManagerProtocols) {
		setUserProp(Settable.JOBMANAGER_PROTOCOLS, Arrays.toString(jobManagerProtocols));
		this.jobManagerProtocols = Arrays.copyOf(jobManagerProtocols, jobManagerProtocols.length);
	}

	public void setPopJavaDaemonPort(int popJavaDaemonPort) {
		setUserProp(Settable.POP_JAVA_DEAMON_PORT, popJavaDaemonPort);
		this.popJavaDaemonPort = popJavaDaemonPort;
	}

	public void setProtocolsWhitelist(Set<String> protocolsWhitelist) {
		setUserProp(Settable.PROTOCOLS_WHITELIST, protocolsWhitelist);
		this.protocolsWhitelist.clear();
		this.protocolsWhitelist.addAll(protocolsWhitelist);
	}

	public void setProtocolsBlacklist(Set<String> protocolsBlacklist) {
		setUserProp(Settable.PROTOCOLS_BLACKLIST, protocolsBlacklist);
		this.protocolsBlacklist.clear();
		this.protocolsBlacklist.addAll(protocolsBlacklist);
	}

	private <T> void setUserProp(Settable prop, T value) {
		if (value == null) {
			USER_PROPERTIES.remove(prop.name());
		} else {
			USER_PROPERTIES.put(prop.name(), String.valueOf(value));
		}
	}

	/**
	 * Load a custom configuration file on top of the system and defaults one.
	 * Hierarchy: User Machine POP Defaults
	 * 
	 * @param file
	 *            The properties file to load
	 * @throws java.io.IOException
	 *             if the given file fail to load
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
				String[] keys = key.split("\\.");
				String value = ALL_PROPERTIES.getProperty(key);

				// get enum
				Settable keyEnum;
				try {
					keyEnum = Settable.valueOf(keys[0].toUpperCase());
				} catch (IllegalArgumentException e) {
					if (debug) {
						System.out.format("[Configuration] unknown key '%s'\n", key);
					}
					continue;
				}

				try {
					switch (keyEnum) {
					case SYSTEM_JOBMANAGER_CONFIG:
						systemJobManagerConfig = new File(value);
						break;
					case DEBUG:
						if (ENV_DEBUG == null) {
							debug = Boolean.parseBoolean(value);
							break;
						}
					case DEBUG_COMBOX:
						debugCombox = Boolean.parseBoolean(value);
						break;
					case RESERVE_TIMEOUT:
						reserveTimeout = Integer.parseInt(value);
						break;
					case ALLOC_TIMEOUT:
						allocTimeout = Integer.parseInt(value);
						break;
					case CONNECTION_TIMEOUT:
						connectionTimeout = Integer.parseInt(value);
						break;
					case JOBMANAGER_UPDATE_INTERVAL:
						jobManagerUpdateInterval = Integer.parseInt(value);
						break;
					case JOBMANAGER_SELF_REGISTER_INTERVAL:
						jobManagerSelfRegisterInterval = Integer.parseInt(value);
						break;
					case JOBMANAGER_DEFAULT_CONNECTOR:
						jobManagerDefaultConnector = value;
						break;
					case JOBMANAGER_EXECUTION_BASE_DIRECTORY:
						jobManagerExecutionBaseDirectory = value;
						break;
					case JOBMANAGER_EXECUTION_USER:
						jobmanagerExecutionUser = value;
						break;
					case JOBMANAGER_PORTS:
						String[] ports = matchRegEx(value, "\\d+");
						jobManagerPorts = new int[ports.length];
						for (int i = 0; i < ports.length; i++) {
							jobManagerPorts[i] = Integer.parseInt(ports[i]);
						}
						break;
					case JOBMANAGER_PROTOCOLS:
						jobManagerProtocols = matchRegEx(value, "[\\w\\d]+");
						break;
					case POP_JAVA_DEAMON_PORT:
						popJavaDaemonPort = Integer.parseInt(value);
						break;
					case SEARCH_NODE_UNLOCK_TIMEOUT:
						searchNodeUnlockTimeout = Integer.parseInt(value);
						break;
					case SEARCH_NODE_SEARCH_TIMEOUT:
						searchNodeSearchTimeout = Integer.parseInt(value);
						break;
					case SEARCH_NODE_MAX_REQUESTS:
						searchNodeMaxRequests = Integer.parseInt(value);
						break;
					case SEARCH_NODE_EXPLORATION_QUEUE_SIZE:
						searchNodeExplorationQueueSize = Integer.parseInt(value);
						break;
					case TFC_SEARCH_TIMEOUT:
						tfcSearchTimeout = Integer.parseInt(value);
						break;
					case DEFAULT_ENCODING:
						defaultEncoding = value;
						break;
					case SELECTED_ENCODING:
						selectedEncoding = value;
						break;
					case DEFAULT_PROTOCOL:
						defaultProtocol = value.toUpperCase();
						break;
					case DEFAULT_NETWORK:
						defaultNetwork = value.toLowerCase();
						break;
					case ALLOCATE_PORT_RANGE:
						allocatePortRange = Integer.parseInt(value);
						break;
					case PROTOCOLS_WHITELIST:
						protocolsWhitelist.clear();
						protocolsWhitelist.addAll(Arrays.asList(matchRegEx(value, "[\\w\\d]+")));
						break;
					case PROTOCOLS_BLACKLIST:
						protocolsBlacklist.clear();
						protocolsBlacklist.addAll(Arrays.asList(matchRegEx(value, "[\\w\\d]+")));
						break;
					case ASYNC_CONSTRUCTOR:
						asyncConstructor = Boolean.parseBoolean(value);
						break;
					case ACTIVATE_JMX:
						activateJmx = Boolean.parseBoolean(value);
						break;
					case CONNECT_TO_POPCPP:
						connectToPOPcpp = Boolean.parseBoolean(value);
						break;
					case CONNECT_TO_JAVA_JOBMANAGER:
						connectToJavaJobmanager = Boolean.parseBoolean(value);
						break;
					case REDIRECT_OUTPUT_TO_ROOT:
						redirectOutputToRoot = Boolean.parseBoolean(value);
						break;
					case USE_NATIVE_SSH_IF_POSSIBLE:
						useNativeSSHifPossible = Boolean.parseBoolean(value);
						break;
					case SSL_PROTOCOL_VERSION:
						SSLProtocolVersion = value;
						break;
					case SSL_KEY_STORE_FILE:
						SSLKeyStoreOptions.setKeyStoreFile(new File(value));
						break;
					case SSL_KEY_STORE_PASSWORD:
						SSLKeyStoreOptions.setKeyStorePassword(value);
						break;
					case SSL_KEY_STORE_PRIVATE_KEY_PASSWORD:
						SSLKeyStoreOptions.setPrivateKeyPassword(value);
						break;
					case SSL_KEY_STORE_FORMAT:
						SSLKeyStoreOptions.setKeyStoreFormat(KeyStoreFormat.valueOf(value));
						break;
					}
				} catch (NumberFormatException e) {
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
	 * Save the configuration to a new properties file, the file is defined in
	 * {@link #setUserConfig(File)}
	 * 
	 * @throws IOException
	 *             if we fail to write the file to disk
	 */
	public void store() throws IOException {
		Objects.requireNonNull(userConfig, "A user configuration file must be provided via setUserConfig or load.");
		File file = userConfig;

		try (PrintStream out = new PrintStream(file)) {
			USER_PROPERTIES.store(out, "Automatically generated by POP-Java");
		}
	}

	/**
	 * Dump configuration to system location, may not work if rights block writing.
	 * 
	 * @throws java.io.IOException
	 *             if we fail to override the system configuration
	 */
	public void writeSystemConfiguration() throws IOException {
		Properties dump = new Properties();
		dump.putAll(ALL_PROPERTIES);
		dump.putAll(USER_PROPERTIES);
		try (PrintStream out = new PrintStream(SYSTEM_CONFIG)) {
			dump.store(out, "Automatically generated by POP-Java");
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
