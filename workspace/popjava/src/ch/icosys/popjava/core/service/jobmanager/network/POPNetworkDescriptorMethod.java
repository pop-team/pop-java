package ch.icosys.popjava.core.service.jobmanager.network;

import java.util.List;

/**
 * The methods which will be use from a descriptor to create the implementation
 * or generic POPConnector and POPNode.
 * 
 * @author Davide Mazzoleni
 */
public interface POPNetworkDescriptorMethod {

	POPConnector createConnector();

	POPNode createNode(List<String> params);

}
