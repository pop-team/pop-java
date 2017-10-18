package popjava.service.jobmanager.connector;

import popjava.util.LogWriter;

/**
 * Create a Protocol to createObject in the JobManager, this method should only be called when creating a Network
 *
 * @see popjava.service.jobmanager.network.POPNetwork
 * @see popjava.service.jobmanager.POPJavaJobManager
 * @author Davide Mazzoleni
 */
public class POPConnectorFactory {
	public static POPConnector makeConnector(String connectorName) {
		try {
			POPConnector.Name connector = POPConnector.Name.from(connectorName);
			return makeConnector(connector);
		} catch(IllegalArgumentException e) {
			LogWriter.writeDebugInfo("[Connector Factory] unknown connector '%s'", connectorName);
		}
		return null;
	}
	
	public static POPConnector makeConnector(POPConnector.Name connector) {
		switch (connector) {
			// network and job manager are passed manually afterwards
			case JOBMANAGER: return new POPConnectorJobManager();
			case DIRECT: return new POPConnectorDirect();
			case TFC: return new POPConnectorTFC();
		}
		return null;
	}
}
