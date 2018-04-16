package popjava.service.jobmanager.network;

import java.util.List;

/**
 * A JobManager node
 *
 * @author Davide Mazzoleni
 */
public class POPNodeJobManager extends POPNodeAJobManager {

	public POPNodeJobManager(String host, int port, String protocol) {
		super(POPConnectorJobManager.DESCRIPTOR, host, port, protocol);

	}
	
	public POPNodeJobManager(List<String> params) {
		super(POPConnectorJobManager.DESCRIPTOR, params);
	}
}
