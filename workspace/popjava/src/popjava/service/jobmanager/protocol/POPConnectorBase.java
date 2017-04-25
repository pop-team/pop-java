package popjava.service.jobmanager.protocol;

import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.network.POPNetwork;
import popjava.service.jobmanager.network.POPNetworkNode;

/**
 *
 * @author Davide Mazzoleni
 */
public abstract class POPConnectorBase {
	
	protected POPNetwork network;
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
	public abstract boolean isValidNode(POPNetworkNode node);
	
	/**
	 * Set this protocol network of influence, will be used by its children
	 * @param network 
	 */
	public void setNetwork(POPNetwork network) {
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
