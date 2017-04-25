package popjava.service.jobmanager.search;

import java.util.HashSet;
import java.util.Set;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

/**
 * Exploration list on the network in the grid.
 * Keep track of each node to avoid infinite loops.
 * @author Davide Mazzoleni
 */
public class SNExploration implements IPOPBase {
	private final Set<POPAccessPoint> visited = new HashSet<>();

	public SNExploration() {
	}

	public SNExploration(SNExploration explorationList) {
		for (POPAccessPoint e : explorationList.visited)
			add(e);
	}

	public boolean contains(POPAccessPoint o) {
		return visited.contains(o);
	}
	
	public boolean contains(String o) {
		return visited.contains(new POPAccessPoint(o));
	}

	public boolean add(POPAccessPoint e) {
		return visited.add(e);
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putInt(visited.size());
		visited.forEach((e) -> buffer.putValue(e, POPAccessPoint.class));
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