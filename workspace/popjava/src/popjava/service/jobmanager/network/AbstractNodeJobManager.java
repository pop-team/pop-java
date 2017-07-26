package popjava.service.jobmanager.network;

import popjava.PopJava;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.POPString;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.connector.POPConnectorBase;

/**
 * Nodes using the Job Manager should extend this class instead
 * 
 * @author Davide Mazzoleni
 * @param <T> A POP Connector implementation
 */
public abstract class AbstractNodeJobManager<T extends POPConnectorBase> extends POPNetworkNode<T> {

	protected POPAccessPoint jobManagerAccessPoint;
	private POPJavaJobManager jm;
	
	public AbstractNodeJobManager(String connectorName, Class<T> connectorClass) {
		super(connectorName, connectorClass);
	}
	
    public POPAccessPoint getJobManagerAccessPoint() {
		return jobManagerAccessPoint;
	}

	public POPJavaJobManager getJobManager() {
		// create connection if it doesn't exists
		if (jm == null) {
			jm = PopJava.newActive(POPJavaJobManager.class, getJobManagerAccessPoint());
		}
		// test connection
		try {
			POPString val = new POPString();
			jm.query("power", val);
		} catch (Exception e) {
			jm = PopJava.newActive(POPJavaJobManager.class, getJobManagerAccessPoint());
		}
		return jm;
	}
}
