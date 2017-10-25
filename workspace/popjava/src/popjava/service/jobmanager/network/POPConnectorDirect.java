package popjava.service.jobmanager.network;

import java.util.ArrayList;
import java.util.List;
import popjava.baseobject.ConnectionType;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.interfacebase.Interface;

/**
 *
 * @author Davide Mazzoleni
 */
public class POPConnectorDirect extends POPConnector {
	
	private static class DescriptorMethodImpl implements POPNetworkDescriptorMethod {
		@Override
		public POPConnector createConnector() {
			return new POPConnectorDirect();
		}

		@Override
		public POPNode createNode(List<String> params) {
			return new POPNodeDirect(new ArrayList<>(params));
		}
	}
	static final POPNetworkDescriptor DESCRIPTOR = new POPNetworkDescriptor("direct", new DescriptorMethodImpl());

	public static final String OD_SERVICE_PORT = "_service-port";

	public POPConnectorDirect() {
		super(DESCRIPTOR);
	}
	
	@Override
	public int createObject(POPAccessPoint localservice, String objname, ObjectDescription od,
			int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		// node in network
		// get a random node
		POPNodeDirect node = (POPNodeDirect) nodes.get((int) (Math.random() * nodes.size()));

		// set od hostname to connect directly
		od.setHostname(node.getHost());
		od.setValue(OD_SERVICE_PORT, node.getPort() + "");
		od.setConnectionType(ConnectionType.SSH);
		od.setConnectionSecret(node.getDaemonSecret());
		// use daemon if necessary
		if (node.isDaemon()) {
			od.setConnectionType(ConnectionType.DAEMON);
		}

		// do n times on the same node
		for (int i = 0; i < howmany; i++) {
			boolean success = Interface.tryLocal(objname, objcontacts[i], od);
		}
		return 0;
	}
}
