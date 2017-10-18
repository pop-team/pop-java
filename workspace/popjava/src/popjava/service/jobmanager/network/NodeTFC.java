package popjava.service.jobmanager.network;

import java.util.List;
import popjava.service.jobmanager.connector.POPConnector;

/**
 * A TFC node
 * 
 * @author Davide Mazzoleni
 */
public class NodeTFC extends AbstractNodeJobManager {

	public NodeTFC(String host, int port, String protocol) {
		super(POPConnector.Name.TFC, host, port, protocol);
	}

	public NodeTFC(List<String> params) {
		super(POPConnector.Name.TFC, params);
	}

}
