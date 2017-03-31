package popjava.service.jobmanager.network;

import popjava.service.jobmanager.protocol.CreateObjectProtocolBase;
import popjava.service.jobmanager.protocol.ProtocolJobManager;
import popjava.service.jobmanager.protocol.ProtocolSSH;

/**
 *
 * @author Davide Mazzoleni
 */
public class NetworkNodeFactory {
	public static NetworkNode makeNode(Class<? extends CreateObjectProtocolBase> aClass, String[] other) {
		// for JobManager 
		if (aClass == ProtocolJobManager.class) {
			return new NodeJobManager(other);
		}
		
		// for direct IP connect
		else if (aClass == ProtocolSSH.class) {
			return new NodeSSH(other);
		}
		
		else {
			// TODO reflection node creation
			return null;
		}
	}
}
