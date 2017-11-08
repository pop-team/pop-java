package popjava.combox;

import java.io.IOException;
import popjava.broker.Broker;
import popjava.buffer.POPBuffer;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.util.Configuration;

/**
 * This abstract class regroup the method needed by a ComboxFactory
 */
public abstract class ComboxFactory {

	/**
	 * Create a new client combox with the given access point
	 * @param networkUUID	The network we are using
	 * @return	The combox created
	 * @throws java.io.IOException
	 */
	public abstract Combox createClientCombox(String networkUUID) throws IOException;

	/**
	 * Create a new server combox with the given access point, buffer and broker
	 * @param accessPoint	The access point for the server
	 * @param buffer		The buffer for sending and receiving
	 * @param broker		The broker associated with this combox
	 * @return	The combox server created	
	 * @throws java.io.IOException	
	 */
	public abstract ComboxServer createServerCombox(AccessPoint accessPoint,
			POPBuffer buffer, Broker broker) throws IOException;

	/**
	 * Create a new server combox with the given access point, buffer and broker and a connection timeout
	 * @param accessPoint	The access point for the server
	 * @param timeout		The connection timeout
	 * @param buffer		The buffer for sending and receiving
	 * @param broker		The broker associated with this combox
	 * @return	The combox server created	
	 * @throws java.io.IOException	
	 */
	public abstract ComboxServer createServerCombox(AccessPoint accessPoint,
			int timeout, POPBuffer buffer, Broker broker) throws IOException;

	/**
	 * Create and allocation combox when instantiating a new remote object
	 * @return 
	 * @throws java.io.IOException 
	 */
	public abstract ComboxAllocate createAllocateCombox() throws IOException;
	
	/**
	 * Get the combox name
	 * @return name of the combox
	 */
	public abstract String getComboxName();
	
	/**
	 * Mark if the protocol used by this Combox is secure or not
	 * @return 
	 */
	public abstract boolean isSecure();
	
	/**
	 * Check if this combox is usable.
	 * When overriding call super.isAvailable to appy the white/black lists.
	 * NB: If an item is in both white and black lists, the blacklist will take precedence
	 * 
	 * Expression applyied: (a V b) Λ ¬c 
	 *  a = whitelist is empty
	 *  b = is in whitelist
	 *  c = is in blacklist
	 * 
	 * Truth table:
	 *  a	b	c		(a V b) Λ ¬c 
	 *  T	T	T		- 
	 *  T	T	F		-
	 *  T	F	T		F  : blacklist
	 *  T	F	F		T  : no restrictions
	 *  F	T	T		F  : blacklist take precedence
	 *  F	T	F		T  : whitelist
	 *  F	F	T		F  : not in whitelist + blacklist
	 *  F	F	F		F  : not in whitelist
	 *  
	 * @return true if it's usable
	 */
	public boolean isAvailable() {
		Configuration conf = Configuration.getInstance();
		return 
			(   conf.getProtocolsWhitelist().isEmpty() 
			 || conf.getProtocolsWhitelist().contains(getComboxName())
			)
			&& !conf.getProtocolsBlacklist().contains(getComboxName());
	}
}
