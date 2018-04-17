package ch.icosys.popjava.core.combox.plugin;

import ch.icosys.popjava.core.baseobject.AccessPoint;
import ch.icosys.popjava.core.broker.Broker;
import ch.icosys.popjava.core.combox.ComboxServer;

/**
 * This class defined the interface for all new combox server plug-in
 */
public class ComboxServerPlugin extends ComboxServer {

	/**
	 * Default constructor. Create a new combox server plug-in
	 * 
	 * @param accessPoint
	 *            Access point of the combox server
	 * @param timeout
	 *            Connection timeout
	 * @param broker
	 *            Associated broker
	 */
	public ComboxServerPlugin(AccessPoint accessPoint, int timeout, Broker broker) {
		super(accessPoint, timeout, broker);
	}

	@Override
	public void close() {
	}

}
