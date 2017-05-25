package popjava.service.jobmanager.connector;

/**
 * Create a Protocol to createObject in the JobManager, this method should only be called when creating a Network
 * @see popjava.service.jobmanager.network.Network
 * @see popjava.service.jobmanager.POPJavaJobManager
 * @author Davide Mazzoleni
 */
public class POPConnectorFactory {
	public static POPConnectorBase makeConnector(String name) {
		switch (name.toLowerCase()) {
			// network and job manager are passed manually afterwards
			case POPConnectorJobManager.IDENTITY: return new POPConnectorJobManager();
			case POPConnectorDirect.IDENTITY: return new POPConnectorDirect();
			case POPConnectorTFC.IDENTITY: return new POPConnectorTFC();
			default: // TODO look for other classes or factory with reflection
				return null;
		}
	}
	public static Class<? extends POPConnectorBase> getConnectorClass(String name) {
		switch (name.toLowerCase()) {
			// network and job manager are passed manually afterwards
			case POPConnectorJobManager.IDENTITY: return POPConnectorJobManager.class;
			case POPConnectorDirect.IDENTITY: return POPConnectorDirect.class;
			case POPConnectorTFC.IDENTITY: return POPConnectorTFC.class;
			default: // TODO look for other classes or factory with reflection
				return null;
		}
	}
}
