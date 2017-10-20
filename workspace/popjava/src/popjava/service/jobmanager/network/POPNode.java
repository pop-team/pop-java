package popjava.service.jobmanager.network;

import java.util.Arrays;

/**
 * A generic node which will be assigned to a specific POP Connector.
 * 
 * @author Davide Mazzoleni
 */
public abstract class POPNode {

	protected String host;
	protected String[] creationParams;
	
	protected final POPNetworkDescriptor descriptor;
	
	protected boolean temporary = false;

	public POPNode(POPNetworkDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * The class for this connector
	 *
	 * @return
	 */
	public POPNetworkDescriptor getConnectorDescriptor() {
		return descriptor;
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
	 * The necessary parameters to create this node via {@link POPNodeFactory#makeNode}
	 *
	 * @return
	 */
	public String[] getCreationParams() {
		return Arrays.copyOf(creationParams, creationParams.length);
	}
}
