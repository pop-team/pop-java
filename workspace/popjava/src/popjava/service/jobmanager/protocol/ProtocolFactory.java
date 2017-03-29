package popjava.service.jobmanager.protocol;

/**
 *
 * @author Davide Mazzoleni
 */
public class ProtocolFactory {
	public static CreateObjectProtocolBase makeProtocol(String name) {
		switch (name.toLowerCase()) {
			// TODO fill with constructor
			case "jobmanager": return new ProtocolJobManager();
			case "ssh": return new ProtocolSSH();
			default: // TODO look for other classes or factory with reflection
				return null;
		}
	}
}
