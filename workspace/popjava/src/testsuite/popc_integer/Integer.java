package testsuite.popc_integer;


import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;

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
