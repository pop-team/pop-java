package popjava.service.jobmanager.connector;

import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.search.SNExploration;
import popjava.service.jobmanager.search.SNRequest;

/**
 * Mark Connectors that works with the JobManager SearchNode methods
 * @author Davide Mazzoleni
 */
public interface POPConnectorSearchNodeInterface {
	void askResourcesDiscoveryAction(SNRequest request, POPAccessPoint sender, SNExploration oldExplorationList);
	boolean broadcastPresence();
}
