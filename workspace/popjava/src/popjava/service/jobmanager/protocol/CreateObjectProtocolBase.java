package popjava.service.jobmanager.protocol;

import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.network.Network;
import popjava.service.jobmanager.network.NetworkNode;

/**
 *
 * @author Davide Mazzoleni
 */
public abstract class CreateObjectProtocolBase {
	
	protected Network network;
	protected POPJavaJobManager jobManager;
	
	/**
	 * Protocol specific createObject
	 * @see POPJavaJobManager
	 * @param localservice
	 * @param objname
	 * @param od
	 * @param howmany
	 * @param objcontacts
	 * @param howmany2
	 * @param remotejobcontacts
	 * @return 
	 */
	public abstract int createObject(POPAccessPoint localservice,
			String objname,
			ObjectDescription od,
			int howmany, POPAccessPoint[] objcontacts,
			int howmany2, POPAccessPoint[] remotejobcontacts);

	/**
	 * Protocol need to check if a node is valid and can handle it
	 * @param node The node to check
	 * @return true if it can be handle, false otherwise
	 */
	public abstract boolean isValidNode(NetworkNode node);
	
	/**
	 * Set this protocol network of influence, will be used by its children
	 * @param network 
	 */
	public void setNetwork(Network network) {
		this.network = network;
	}

	/**
	 * Reference to the JobManger, needed for potential call to Reserve, CancelReservation, etc.
	 * @param jobManager 
	 */
	public void setJobManager(POPJavaJobManager jobManager) {
		this.jobManager = jobManager;
	}
}
