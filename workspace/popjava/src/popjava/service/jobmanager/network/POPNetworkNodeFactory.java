package popjava.service.jobmanager.network;

import popjava.service.jobmanager.protocol.POPConnectorBase;
import popjava.service.jobmanager.protocol.POPConnectorJobManager;
import popjava.service.jobmanager.protocol.POPConnectorDirect;

/**
 *
 * @author Davide Mazzoleni
 */
public class POPNetworkNodeFactory {
	public static POPNetworkNode makeNode(Class<? extends POPConnectorBase> aClass, String[] other) {
		// for JobManager 
		if (aClass == POPConnectorJobManager.class) {
			return new NodeJobManager(other);
		}
		
		// for direct IP connect
		else if (aClass == POPConnectorDirect.class) {
			return new NodeDirect(other);
		}
		
		else {
			// TODO reflection node creation
			return null;
		}
	}
}
