package ch.icosys.popjava.core.service.jobmanager.network;

import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.service.jobmanager.search.SNExploration;
import ch.icosys.popjava.core.service.jobmanager.search.SNRequest;

/**
 * Mark Connectors that works with the JobManager SearchNode methods
 * 
 * @author Davide Mazzoleni
 */
public interface POPConnectorSearchNodeInterface {
	void askResourcesDiscoveryAction(SNRequest request, POPAccessPoint sender, SNExploration oldExplorationList);

	boolean broadcastPresence();
}
