package popjava.service.jobmanager.network;

import java.util.List;
import popjava.service.jobmanager.connector.POPConnector;

/**
 * A JobManager node
 *
 * @author Davide Mazzoleni
 */
public class NodeJobManager extends AbstractNodeJobManager {

	public NodeJobManager(String host, int port, String protocol) {
		super(POPConnector.Name.JOBMANAGER, host, port, protocol);

	}
	
	NodeJobManager(List<String> params) {
		super(POPConnector.Name.JOBMANAGER, params);
	}
}
