package ch.icosys.popjava.core.service.jobmanager.search;

import java.util.LinkedList;

import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.buffer.POPBuffer;
import ch.icosys.popjava.core.dataswaper.IPOPBase;
import ch.icosys.popjava.core.util.Configuration;

/**
 * Exploration list on the network in the grid. Keep track of each node to avoid
 * infinite loops.
 *
 * @author Davide Mazzoleni
 */
public class SNExploration implements IPOPBase {

	private final LinkedList<POPAccessPoint> visited = new LinkedList<>();

	private final Configuration conf = Configuration.getInstance();

	public SNExploration() {
	}

	public SNExploration(SNExploration explorationList) {
		for (POPAccessPoint e : explorationList.visited) {
			add(e);
		}
	}

	public boolean contains(POPAccessPoint o) {
		return visited.contains(o);
	}

	public boolean contains(String o) {
		return visited.contains(new POPAccessPoint(o));
	}

	public boolean add(POPAccessPoint e) {
		if (visited.size() >= conf.getSearchNodeExplorationQueueSize()) {
			visited.pop();
		}
		return visited.add(e);
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putInt(visited.size());
		for (POPAccessPoint ap : visited) {
			ap.serialize(buffer);
		}
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		int size = buffer.getInt();
		for (int i = 0; i < size; i++) {
			visited.add((POPAccessPoint) buffer.getValue(POPAccessPoint.class));
		}
		return true;
	}
}
