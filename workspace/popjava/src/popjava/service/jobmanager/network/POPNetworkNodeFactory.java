package popjava.service.jobmanager.network;

import popjava.service.jobmanager.protocol.POPProtocolBase;
import popjava.service.jobmanager.protocol.POPProtocolJobManager;
import popjava.service.jobmanager.protocol.POPProtocolDirect;

/**
 *
 * @author Davide Mazzoleni
 */
public class POPNetworkNodeFactory {
	public static POPNetworkNode makeNode(Class<? extends POPProtocolBase> aClass, String[] other) {
		// for JobManager 
		if (aClass == POPProtocolJobManager.class) {
			return new NodeJobManager(other);
		}
		
		// for direct IP connect
		else if (aClass == POPProtocolDirect.class) {
			return new NodeDirect(other);
		}
		
		else {
			// TODO reflection node creation
			return null;
		}
	}
}
