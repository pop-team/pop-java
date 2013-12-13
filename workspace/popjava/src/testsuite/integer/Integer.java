package testsuite.integer;

import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncMutex;
import popjava.base.*;

@POPClass
public class Integer extends POPObject{
	protected int value;
	
	@POPObjectDescription(url = "localhost")
	public Integer(){
		value = 10;
	}
	
	@POPSyncMutex
	public int get(){
		return this.value;
	}
	
	@POPSyncMutex
	public void add(Integer i){
		int val = i.get();
		this.value+=val;
	}
	
	@POPAsyncSeq
	public void set(int val){
		this.value=val;
	}
}
