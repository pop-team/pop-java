package popjava.service.jobmanager.connector;

import java.util.List;
import popjava.baseobject.ConnectionType;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.interfacebase.Interface;
import popjava.service.jobmanager.network.POPNetworkNode;
import popjava.service.jobmanager.network.NodeDirect;

/**
 *
 * @author Davide Mazzoleni
 */
public class POPConnectorDirect extends POPConnectorBase {

	public static final String IDENTITY = "direct";

	@Override
	public int createObject(POPAccessPoint localservice, String objname, ObjectDescription od,
			int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		// node in network
		List<NodeDirect> nodes = network.getMembers(this.getClass());
		// get a random node
		NodeDirect node = nodes.get((int) (Math.random() * nodes.size()));

		// set od hostname to connect directly
		od.setHostname(node.getHost());
		od.setValue("port", node.getPort() + "");
		od.setConnectionType(ConnectionType.SSH);
		od.setConnectionSecret(node.getDaemonSecret());
		// use daemon if necessary
		if (node.isDaemon()) {
			od.setConnectionType(ConnectionType.DEAMON);
		}

		// do n times on the same node
		for (int i = 0; i < howmany; i++) {
			boolean success = Interface.tryLocal(objname, objcontacts[i], od);
		}
		return 0;
	}

	@Override
	public boolean isValidNode(POPNetworkNode node) {
		return node instanceof NodeDirect && ((NodeDirect) node).isInitialized();
	}

}
