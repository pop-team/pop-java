import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.annotation.POPSyncSeq;

@POPClass
public class Integer {
	protected int value;
	
	public Integer(){
	}
	
	@POPSyncConc
	public int get(){
		return this.value;
	}
	
	@POPSyncMutex
	public void add(Integer i) {
		 value += i.get();
	}
	
	@POPSyncSeq
	public void set(int val){
		this.value=val;
	}
}

