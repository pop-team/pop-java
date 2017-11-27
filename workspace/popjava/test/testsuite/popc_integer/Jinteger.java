package testsuite.popc_integer;

import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;

@POPClass(classId = 1001, deconstructor = true)
public class Jinteger{
	private int value;
	
	public Jinteger(){
		//TODO: implement power
		//od.setPower(100, 10);
	}
	
	@POPSyncConc
	public int jget() {
		return value;
	}

	@POPAsyncSeq
	public void jset(int value) {
		this.value=value;
	}

	@POPSyncMutex
	public void jadd(Jinteger other) {
		this.value += other.jget();
//		try {
//			Jinteger i = (Jinteger)PopJava.newActive(Jinteger.class, other);
//			int value = i.jget();
//			this.value+=value;
//		} catch (POPException e) {
//			e.printStackTrace();
//		}	
	}
	
	@POPSyncMutex
	public void add(Integer other){
		this.value += other.get();
	}
}
