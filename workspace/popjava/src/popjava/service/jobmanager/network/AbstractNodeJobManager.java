package popjava.service.jobmanager.network;

import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.connector.POPConnectorBase;

/**
 * Nodes using the Job Manager should extend this class instead
 * 
 * @author Davide Mazzoleni
 * @param <T> A POP Connector implementation
 */
public abstract class AbstractNodeJobManager<T extends POPConnectorBase> extends POPNetworkNode<T> {

	protected POPAccessPoint jobManagerAccessPoint;
	
	public AbstractNodeJobManager(String connectorName, Class<T> connectorClass) {
		super(connectorName, connectorClass);
	}
	
    public POPAccessPoint getJobManagerAccessPoint() {
		return  jobManagerAccessPoint;
	}
}
