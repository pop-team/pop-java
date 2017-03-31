package popjava.service.jobmanager.protocol;

/**
 * Create a Protocol to createObject in the JobManager, this method should only be called when creating a Network
 * @see popjava.service.jobmanager.network.Network
 * @see popjava.service.jobmanager.POPJavaJobManager
 * @author Davide Mazzoleni
 */
public class ProtocolFactory {
	public static CreateObjectProtocolBase makeProtocol(String name) {
		switch (name.toLowerCase()) {
			// network and job manager are passed manually afterwards
			case "jobmanager": return new ProtocolJobManager();
			case "ssh": return new ProtocolSSH();
			default: // TODO look for other classes or factory with reflection
				return null;
		}
	}
}
