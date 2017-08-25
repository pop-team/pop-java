package popjava.service.jobmanager.network;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.connector.POPConnectorJobManager;
import popjava.serviceadapter.POPJobManager;
import popjava.util.Configuration;
import popjava.util.Util;

/**
 * A JobManager node
 *
 * @author Davide Mazzoleni
 */
public class NodeJobManager extends AbstractNodeJobManager<POPConnectorJobManager> {

	private int port;
	private String protocol;
	private boolean initialized = true;

	public NodeJobManager(String host, int port, String protocol) {
		super(POPConnectorJobManager.IDENTITY, POPConnectorJobManager.class);
		this.host = host;
		this.port = port;
		this.protocol = protocol;
		
		init();
	}
	
	/**
	 * 
	 *
	 * @param params List with parameters
	 */
	NodeJobManager(List<String> params) {
		super(POPConnectorJobManager.IDENTITY, POPConnectorJobManager.class);

		// get potential params
		host = Util.removeStringFromList(params, "host=");
		String portString = Util.removeStringFromList(params, "port=");
		protocol= Util.removeStringFromList(params, "protocol=");
		
		// stop if we have no host
		if (host == null) {
			initialized = false;
			return;
		}
		
		if (protocol == null) {
			protocol = Configuration.getDefaultProtocol();
		}

		// some sane defaults
		port = POPJobManager.DEFAULT_PORT;
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
		// set access point
		jobManagerAccessPoint = new POPAccessPoint(String.format("%s://%s:%d", protocol, host, port));

		Set<String> paramsSet = new HashSet<>();
		paramsSet.add("connector=" + POPConnectorJobManager.IDENTITY);
		paramsSet.add("host=" + host);
		paramsSet.add("port=" + port);
		paramsSet.add("protocol=" + protocol);
		creationParams = paramsSet.toArray(new String[0]);
	}

	@Override
	public POPAccessPoint getJobManagerAccessPoint() {
		return jobManagerAccessPoint;
	}

	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode(this.host);
		hash = 97 * hash + this.port;
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
		final NodeJobManager other = (NodeJobManager) obj;
		if (this.port != other.port) {
			return false;
		}
		if (!Objects.equals(this.host, other.host)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("host=%s port=%s connector=%s protocol=%s", host, port, connectorName, protocol);
	}
}
