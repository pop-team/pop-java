package popjava.service.jobmanager.protocol;

/**
 * Create a Protocol to createObject in the JobManager, this method should only be called when creating a Network
 * @see popjava.service.jobmanager.network.Network
 * @see popjava.service.jobmanager.POPJavaJobManager
 * @author Davide Mazzoleni
 */
public class POPProtocolFactory {
	public static POPProtocolBase makeProtocol(String name) {
		switch (name.toLowerCase()) {
			// network and job manager are passed manually afterwards
			case "jobmanager": return new POPProtocolJobManager();
			case "ssh": return new POPProtocolDirect();
			default: // TODO look for other classes or factory with reflection
				return null;
		}
	}
}
