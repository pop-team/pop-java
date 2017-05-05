package popjava.service.jobmanager.network;

import java.util.List;
import java.util.Objects;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.protocol.POPConnectorJobManager;
import popjava.serviceadapter.POPJobManager;
import popjava.util.Util;

/**
 * A JobManager node
 * @author Davide Mazzoleni
 */
public class NodeJobManager extends POPNetworkNode{

	private POPAccessPoint jobManagerAccessPoint;
	private boolean initialized = true;
	
	/**
	 * Two way to set this:
	 *  case <protocol> <ip> <port> 
	 *  case <access point>
	 * @param params A 1 or 3 elements String array
	 */
	@SuppressWarnings("unchecked")
	NodeJobManager(List<String> params) {
		super(POPConnectorJobManager.IDENTITY, POPConnectorJobManager.class);
		
		// get potential params
		String host = Util.removeStringFromList(params, "host=");
		String portString = Util.removeStringFromList(params, "port=");
		
		// stop if we have no host
		if (host == null) {
			initialized = false;
			return;
		}
		
		// some sane defaults
		int port = POPJobManager.DEFAULT_PORT;
		if (portString != null) {
			try {
				port = Integer.parseInt(portString);
			} catch(NumberFormatException e) {
				// we assume the initialization failed in this case
				initialized = false;
			}
		}
		
		// set access point
		jobManagerAccessPoint = new POPAccessPoint(String.format("%s://%s:%d", AccessPoint.SOCKET_PROTOCOL, host, port));
	}

	public POPAccessPoint getJobManagerAccessPoint() {
		return jobManagerAccessPoint;
	}

	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 29 * hash + Objects.hashCode(this.jobManagerAccessPoint);
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
		if (!Objects.equals(this.jobManagerAccessPoint, other.jobManagerAccessPoint)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String[] vals = jobManagerAccessPoint.toString().split("(://)|:");
		return String.format("host=%s port=%s connector=%s", vals[1], vals[2], connectorName);
	}
}
