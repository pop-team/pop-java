package popjava.service.jobmanager.network;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import popjava.service.jobmanager.connector.POPConnectorDirect;
import popjava.util.Configuration;
import popjava.util.Util;

/**
 * A SSH node for direct IP connections
 *
 * @author Davide Mazzoleni
 */
public class NodeDirect extends POPNetworkNode<POPConnectorDirect> {

	private int port;
	private boolean daemon;
	private String daemonSecret;
	private boolean initialized = true;

	/**
	 * Used for creating daemons direct nodes
	 * 
	 * @param host
	 * @param port
	 * @param daemonSecret 
	 */
	public NodeDirect(String host, int port, String daemonSecret) {
		super(POPConnectorDirect.IDENTITY, POPConnectorDirect.class);
		this.host = host;
		this.port = port;
		this.daemon = true;
		this.daemonSecret = daemonSecret;
		
		init();
	}

	/**
	 * Used for creating SSH nodes
	 * 
	 * @param host
	 * @param port 
	 */
	public NodeDirect(String host, int port) {
		super(POPConnectorDirect.IDENTITY, POPConnectorDirect.class);
		this.host = host;
		this.port = port;
		this.daemon = false;
		this.daemonSecret = null;
		
		init();
	}

	NodeDirect(List<String> params) {
		super(POPConnectorDirect.IDENTITY, POPConnectorDirect.class);

		// get potential params
		String host = Util.removeStringFromList(params, "host=");
		String portString = Util.removeStringFromList(params, "port=");
		String protocol = Util.removeStringFromList(params, "protocol=");
		String secret = Util.removeStringFromList(params, "secret=");

		// stop if we have no host
		if (host == null) {
			initialized = false;
			return;
		}

		// set parameters
		this.host = host;
		this.daemon = protocol != null && protocol.equals("daemon");
		this.daemonSecret = secret;
		// by default 22 if ssh, 43424 if daemon
		this.port = !this.daemon ? 22 : Configuration.getInstance().getPopJavaDeamonPort();
		// port need to be parsed
		if (portString != null) {
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
				// we assume the initialization failed in this case
				initialized = false;
			}
		}
		
		init();
	}
	
	private void init() {
		// set parameters again for future creation and sharing, keep posible extra values
		Set<String> paramsSet = new HashSet<>();
		paramsSet.add("connector=" + POPConnectorDirect.IDENTITY);
		paramsSet.add("host=" + this.host);
		paramsSet.add("port=" + this.port);
		paramsSet.add("protocol=" + (this.daemon ? "daemon" : "ssh"));
		if (daemonSecret != null) {
			paramsSet.add("secret=" + daemonSecret);
		}
		creationParams = paramsSet.toArray(new String[0]);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isDaemon() {
		return daemon;
	}

	public String getDaemonSecret() {
		return daemonSecret;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + Objects.hashCode(this.host);
		hash = 59 * hash + this.port;
		hash = 59 * hash + (this.daemon ? 1 : 0);
		hash = 59 * hash + Objects.hashCode(this.daemonSecret);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NodeDirect other = (NodeDirect) obj;
		if (this.port != other.port) {
			return false;
		}
		if (this.daemon != other.daemon) {
			return false;
		}
		if (!Objects.equals(this.host, other.host)) {
			return false;
		}
		if (!Objects.equals(this.daemonSecret, other.daemonSecret)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("host=%s port=%d connector=%s protocol=%s %s", host, port, connectorName,
				daemon ? "daemon" : "ssh",
				daemonSecret == null ? "" : "secret=" + daemonSecret).trim();
	}
}
