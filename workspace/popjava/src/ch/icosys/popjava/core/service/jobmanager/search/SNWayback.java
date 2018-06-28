package ch.icosys.popjava.core.service.jobmanager.search;

import java.util.LinkedList;

import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.buffer.POPBuffer;
import ch.icosys.popjava.core.dataswaper.IPOPBase;

/**
 * List the AccessPoint of each intermediary node to go back to the origin.
 *
 * @author Davide Mazzoleni
 */
public class SNWayback implements IPOPBase {

	private final LinkedList<POPAccessPoint> stack = new LinkedList<>();

	public SNWayback() {
	}

	public SNWayback(SNWayback wayback) {
		stack.addAll(wayback.stack);
	}

	public boolean isLastNode() {
		return stack.isEmpty();
	}

	public void push(POPAccessPoint e) {
		for(int i = 0; i < e.size(); i++) {
			if(e.get(i).getHost().equals("localhost")) {
				new Exception(e.toString()).printStackTrace();
			}
		}
		stack.push(e);
	}

	public POPAccessPoint pop() {
		return stack.pop();
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putInt(stack.size());
		for (POPAccessPoint p : stack) {
			p.serialize(buffer);
		}
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		int size = buffer.getInt();
		for (int i = 0; i < size; i++) {
			stack.add((POPAccessPoint) buffer.getValue(POPAccessPoint.class));
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (POPAccessPoint e : stack) {
			sb.append(e.toString()).append(" ");
		}
		return sb.toString();
	}
}
