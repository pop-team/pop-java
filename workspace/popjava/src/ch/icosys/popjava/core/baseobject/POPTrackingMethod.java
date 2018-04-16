package ch.icosys.popjava.core.baseobject;

import ch.icosys.popjava.core.buffer.POPBuffer;
import ch.icosys.popjava.core.dataswaper.IPOPBase;

/**
 * This class keep track of the calls toward a specific method.
 * 
 * @author Davide Mazzoleni, Christophe Gisler
 */
public class POPTrackingMethod implements IPOPBase {

	private String method;
	private int calls;
	private long totalTime;
	private long totalInputParamsSize;
	private long totalOutputResultSize;

	public POPTrackingMethod() {
		this(null);
	}

	POPTrackingMethod(String method) {
		this.method = method;
	}

	/**
	 * The method used.
	 * @return the method name
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Total number of calls to this method.
	 * @return total number of calls to the method
	 */
	public long getTotalCalls() {
		return calls;
	}

	/**
	 * Register a new method call.
	 * @param time how much did it takes
	 */
	public synchronized void increment(long time, int inputParamsSize, int outputResultSize) {
		calls++;
		totalTime += time;
		totalInputParamsSize += inputParamsSize;
		totalOutputResultSize += outputResultSize;
	}

	/**
	 * The total time used by this method in milliseconds.
	 * @return total time used
	 */
	public long getTotalTime() {
		return totalTime;
	}
	
	/**
	 * The total size of the input parameters of the method in bytes.
	 * @return total size of the input parameters
	 */
	public long getTotalInputParamsSize() {
	    return totalInputParamsSize;
	}
	
	/**
	 * The total size of the output result object returned by the method (if any) in bytes.
	 * @return total size of the output result object
	 */
	public long getTotalOutputResultSize() {
	    return totalOutputResultSize;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(method);
		buffer.putInt(calls);
		buffer.putLong(totalTime);
		buffer.putLong(totalInputParamsSize);
		buffer.putLong(totalOutputResultSize);
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		method = buffer.getString();
		calls = buffer.getInt();
		totalTime = buffer.getLong();
		totalInputParamsSize = buffer.getLong();
		totalOutputResultSize = buffer.getLong();
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s -> calls: %d, time: %d ms, input size: %d bytes, output size: %d bytes ", 
			method, calls, totalTime, totalInputParamsSize, totalOutputResultSize);
	}
}
