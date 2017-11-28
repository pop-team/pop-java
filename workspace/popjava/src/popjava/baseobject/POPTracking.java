package popjava.baseobject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

/**
 * This class contains information on the caller toward an object.
 * We store how many time a method was called and for how long.
 * 
 * @author Davide Mazzoleni
 */
public class POPTracking implements IPOPBase {

	private String caller;
	private final Map<String, POPTrackingMethod> calls;

	public POPTracking() {
		this(null);
	}

	public POPTracking(String caller) {
		this.caller = caller;
		this.calls = new HashMap<>();
	}

	public String getCaller() {
		return caller;
	}

	public List<POPTrackingMethod> getCalls() {
		POPTrackingMethod[] data = calls.values().toArray(new POPTrackingMethod[calls.size()]);
		return Arrays.asList(data);
	}

	public void track(String method, long time) {
		POPTrackingMethod recorder = calls.get(method);
		if (recorder == null) {
			recorder = new POPTrackingMethod(method);
			calls.put(method, recorder);
		}
		recorder.increment(time);
	}
	
	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(caller);
		buffer.putInt(calls.size());
		for (POPTrackingMethod call : calls.values()) {
			call.serialize(buffer);
		}
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		caller = buffer.getString();
		int size = buffer.getInt();
		for (int i = 0; i < size; i++) {
			POPTrackingMethod method = new POPTrackingMethod();
			method.deserialize(buffer);
			calls.put(method.getMethod(), method);
		}
		return true;
	}
	
}
