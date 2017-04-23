package popjava.service.jobmanager.search;

import java.util.LinkedList;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

/**
 * List the AccessPoint of each intermediary node to go back to the origin.
 * @author Davide Mazzoleni
 */
public class SNWayback implements IPOPBase {
	
	private final LinkedList<POPAccessPoint> stack = new LinkedList<>();

	public boolean isLastNode() {
		return stack.isEmpty();
	}

	public void push(POPAccessPoint e) {
		stack.push(e);
	}

	public POPAccessPoint pop() {
		return stack.pop();
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putInt(stack.size());
		stack.forEach((e) -> buffer.putValue(e, POPAccessPoint.class));
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		int size = buffer.getInt();
		for (int i = 0; i < size; i++) {
			stack.push((POPAccessPoint) buffer.getValue(POPAccessPoint.class));
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (POPAccessPoint e : stack)
			sb.append(e.toString()).append(" ");
		return sb.toString();
	}
}
