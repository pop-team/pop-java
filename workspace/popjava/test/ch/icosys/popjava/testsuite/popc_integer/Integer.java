package ch.icosys.popjava.testsuite.popc_integer;


import ch.icosys.popjava.core.annotation.POPAsyncSeq;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncMutex;

@POPClass(classId = 1500, className = "Integer", deconstructor = true)
public class Integer{
	private int value=0;
	
	
	public Integer() {
	}
	
	@POPSyncConc
	public int get() {
		return value;
	}

	@POPAsyncSeq
	public void set(int value) {
		this.value=value;
	}

	@POPSyncMutex
	public void add(Integer myInteger) {
		this.value += myInteger.get();
	}
}
