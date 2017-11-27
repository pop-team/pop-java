package popjava.baseobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import popjava.base.MethodInfo;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

/**
 * This class keep track of the calls toward a specific method.
 * 
 * @author Davide Mazzoleni
 */
class POPTrackingMethod implements IPOPBase {

	private final MethodInfo method;
	private long timeUsed;

	POPTrackingMethod(MethodInfo method) {
		this.method = method;
	}

	/**
	 * Register a new method call.
	 * @param time 
	 */
	public synchronized void increment(Long time) {
		timeUsed += time;
	}

	/**
	 * The total time used by this method.
	 * @return 
	 */
	public long getTimeUsed() {
		return timeUsed;
	}

	/**
	 * The method used.
	 * @return 
	 */
	public MethodInfo getMethod() {
		return method;
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
