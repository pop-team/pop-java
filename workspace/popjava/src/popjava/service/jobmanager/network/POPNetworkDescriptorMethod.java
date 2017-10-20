package popjava.service.jobmanager.network;

import java.util.List;

/**
 * The methods which will be use from a descriptor to create the implementation or generic POPConnector and POPNode.
 * 
 * @author Davide Mazzoleni
 */
public interface POPNetworkDescriptorMethod {

	public POPConnector createConnector();

	public POPNode createNode(List<String> params);
	
}
