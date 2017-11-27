package popjava.baseobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import popjava.base.MethodInfo;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;
import popjava.util.POPRemoteCaller;

/**
 * This class contains information on the caller toward an object.
 * We store how many time a method was called and for how long.
 * 
 * @author Davide Mazzoleni
 */
public class POPTracking implements IPOPBase {

	// TODO change caller in String (ID)
	private final POPRemoteCaller caller;
	private final Map<MethodInfo, POPTrackingMethod> calls;

	public POPTracking(POPRemoteCaller caller) {
		this.caller = caller;
		this.calls = new HashMap<>();
	}

	public POPRemoteCaller getCaller() {
		return caller;
	}

	public List<POPTrackingMethod> getCalls() {
		return Collections.unmodifiableList(new ArrayList(calls.values()));
	}

	public void track(MethodInfo method, long time) {
		POPTrackingMethod recorder = calls.get(method);
		if (recorder == null) {
			recorder = new POPTrackingMethod(method);
			calls.put(method, recorder);
		}
		recorder.increment(time);
	}
	
	@Override
	public boolean serialize(POPBuffer buffer) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
