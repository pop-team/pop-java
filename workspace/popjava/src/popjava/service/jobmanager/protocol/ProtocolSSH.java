package popjava.service.jobmanager.protocol;

import java.util.List;
import popjava.baseobject.ConnectionType;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.interfacebase.Interface;
import popjava.service.jobmanager.network.NetworkNode;
import popjava.service.jobmanager.network.NodeSSH;

/**
 *
 * @author Davide Mazzoleni
 */
public class ProtocolSSH extends CreateObjectProtocolBase {
	
	@Override
	@SuppressWarnings("empty-statement")
	public int createObject(POPAccessPoint localservice, String objname, ObjectDescription od, 
			int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		// node in network
		List<NodeSSH> nodes = network.getMembers();
		// get a random node
		NodeSSH node = nodes.get( (int) (Math.random() * nodes.size()) );
		
		// set od hostname to connect directly
		od.setHostname(node.getHost());
		od.setValue("port", node.getPort() + "");
		od.setConnectionType(ConnectionType.SSH);
		od.setConnectionSecret(node.getDaemonSecret());
		// use daemon if necessary
		if (node.isDaemon())
			od.setConnectionType(ConnectionType.DEAMON);
		
		// do n times on the same node
		for(int i = 0; i < howmany; i++){
			boolean success = Interface.tryLocal(objname, objcontacts[i], od);
		}
		return 0;
	}

	@Override
	public boolean isValidNode(NetworkNode node) {
		return node instanceof NodeSSH && ((NodeSSH)node).isInitialized();
	}
	
}
