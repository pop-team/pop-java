package popjava.service.jobmanager.network;

import java.util.Objects;
import popjava.baseobject.POPAccessPoint;

/**
 * A JobManager node
 * @author Davide Mazzoleni
 */
public class NodeJobManager extends NetworkNode{

	private final POPAccessPoint jobManagerAccessPoint;
	private boolean initialized = true;
	
	/**
	 * Two way to set this:
	 *  case <protocol> <ip> <port> 
	 *  case <access point>
	 * @param params A 1 or 3 elements String array
	 */
	NodeJobManager(String[] params) {
		if (params.length == 1)
			jobManagerAccessPoint = new POPAccessPoint(params[0]);
		else if (params.length == 3)
			jobManagerAccessPoint = new POPAccessPoint(String.format("%s://%s:%s", params[0], params[1], params[2]));
		else {
			// fail to create
			jobManagerAccessPoint = null;
			initialized = false;
		}
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
}
