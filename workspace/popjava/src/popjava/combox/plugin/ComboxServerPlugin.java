package popjava.combox.plugin;

import popjava.baseobject.AccessPoint;
import popjava.broker.Broker;
import popjava.combox.ComboxServer;

/**
 * This class defined the interface for all new combox server plug-in
 */
public class ComboxServerPlugin extends ComboxServer {

	/**
	 * Default constructor. Create a new combox server plug-in
	 * @param accessPoint	Access point of the combox server
	 * @param timeout		Connection timeout
	 * @param broker		Associated broker
	 */
	public ComboxServerPlugin(AccessPoint accessPoint, int timeout,
			Broker broker) {
		super(accessPoint, timeout, broker);
	}

}
