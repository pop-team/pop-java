import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;

@POPClass(classId = 1000, deconstructor = true)
public class Integer {
	private int value;
	
	public Integer(){
		value = 0;
	}

	@POPSyncConc
	public int get(){
		return value;
	}

	@POPSyncMutex
	public void add(Integer i){
		value += i.get();
	}

	@POPAsyncSeq
	public void set(int val){
		value = val;
	}
}
