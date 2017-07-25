package popjava.combox;

import popjava.broker.Broker;
import popjava.buffer.POPBuffer;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;

/**
 * This abstract class regroup the method needed by a ComboxFactory
 */
public abstract class ComboxFactory {

	/**
	 * Create a new client combox with the given access point
	 * @param accessPoint	The access point to connect the combox
	 * @return	The combox created
	 */
	public abstract Combox createClientCombox(POPAccessPoint accessPoint);

	/**
	 * Create a new client combox with the given access point and a specified timeout
	 * @param accessPoint	The access point to connect the combox
	 * @param timeout		The connection timeout
	 * @return	The combox created
	 */
	public abstract Combox createClientCombox(POPAccessPoint accessPoint,
			int timeout);

	/**
	 * Create a new server combox with the given access point, buffer and broker
	 * @param accessPoint	The access point for the server
	 * @param buffer		The buffer for sending and receiving
	 * @param broker		The broker associated with this combox
	 * @return	The combox server created	
	 */
	public abstract ComboxServer createServerCombox(AccessPoint accessPoint,
			POPBuffer buffer, Broker broker);

	/**
	 * Create a new server combox with the given access point, buffer and broker and a connection timeout
	 * @param accessPoint	The access point for the server
	 * @param timeout		The connection timeout
	 * @param buffer		The buffer for sending and receiving
	 * @param broker		The broker associated with this combox
	 * @return	The combox server created	
	 */
	public abstract ComboxServer createServerCombox(AccessPoint accessPoint,
			int timeout, POPBuffer buffer, Broker broker);

	/**
	 * Create and allocation combox when instantiating a new remote object
	 * @return 
	 */
	public abstract ComboxAllocate createAllocateCombox();
	
	/**
	 * Get the combox name
	 * @return name of the combox
	 */
	public abstract String getComboxName();
	
	/**
	 * Check if this combox is usable
	 * @return true if it's usable
	 */
	public abstract boolean isAvailable();
}
