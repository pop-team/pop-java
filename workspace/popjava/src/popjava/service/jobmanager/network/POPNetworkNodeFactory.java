package popjava.service.jobmanager.network;

import java.util.List;
import popjava.service.jobmanager.protocol.POPConnectorJobManager;
import popjava.service.jobmanager.protocol.POPConnectorDirect;
import popjava.util.Configuration;
import popjava.util.Util;

/**
 *
 * @author Davide Mazzoleni
 */
public class POPNetworkNodeFactory {
	public static POPNetworkNode makeNode(List<String> other) {
		String connector = Util.removeStringFromList(other, "connector=");
		// use job manager if nothing is specified
		if (connector == null) {
			connector = Configuration.DEFAULT_JOBMANAGER_CONNECTOR;
		}
		
		switch (connector.toLowerCase()) {
			case POPConnectorJobManager.IDENTITY: return new NodeJobManager(other);
			case POPConnectorDirect.IDENTITY: return new NodeDirect(other);
			default: return null;
		}
	}
}
