package popjava.service.jobmanager.network;

/**
 *
 * @author Davide Mazzoleni
 */
public class NodeFactory {
	public static Node makeProtocol(String name) {
		switch (name.toLowerCase()) {
			// TODO fill with constructor
			case "jobmanager": return new NodeJobManager();
			case "ssh": return new NodeSSH();
			default: // TODO look for other classes or factory with reflection
				return null;
		}
	}
}
