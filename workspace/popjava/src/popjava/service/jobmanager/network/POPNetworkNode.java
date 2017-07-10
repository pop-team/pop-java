package popjava.service.jobmanager.network;

import java.util.Arrays;
import popjava.service.jobmanager.connector.POPConnectorBase;

/**
 *
 * @author Davide Mazzoleni
 * @param <T> The class type for this node
 */
public abstract class POPNetworkNode<T extends POPConnectorBase> {

	protected final Class<T> connectorClass;
	protected final String connectorName;
	protected String[] creationParams;

	public POPNetworkNode(String connectorName, Class<T> connectorClass) {
		this.connectorName = connectorName;
		this.connectorClass = connectorClass;
	}

	/**
	 * The class for this connector
	 *
	 * @return
	 */
	public Class<T> getConnectorClass() {
		return connectorClass;
	}

	/**
	 * The name used to identify this connector, also used by POPConnectorFactory
	 *
	 * @return
	 */
	public String getConnectorName() {
		return connectorName;
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
