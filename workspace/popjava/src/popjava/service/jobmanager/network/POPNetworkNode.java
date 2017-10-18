package popjava.service.jobmanager.network;

import java.util.Arrays;
import popjava.dataswaper.IPOPBase;
import popjava.service.jobmanager.connector.POPConnector;

/**
 * A generic node which will be assigned to a specific POP Connector.
 * 
 * @author Davide Mazzoleni
 */
public abstract class POPNetworkNode {

	protected String host;
	protected String[] creationParams;
	
	protected final POPConnector.Name name;
	
	protected boolean temporary = false;

	public POPNetworkNode(POPConnector.Name name) {
		this.name = name;
	}

	/**
	 * The class for this connector
	 *
	 * @return
	 */
	public POPConnector.Name getConnectorName() {
		return name;
	}

	/**
	 * Host of the node
	 * 
	 * @return 
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Should this node be store on disk
	 * 
	 * @return 
	 */
	public boolean isTemporary() {
		return temporary;
	}

	/**
	 * Mark node as temporary
	 * 
	 * @param temporary 
	 */
	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}

	/**
	 * The configuration file representation
	 *
	 * @return
	 */
	@Override
	public abstract String toString();

	/**
	 * The necessary parameters to create this node via {@link POPNetworkNodeFactory#makeNode}
	 *
	 * @return
	 */
	public String[] getCreationParams() {
		return Arrays.copyOf(creationParams, creationParams.length);
	}
}
