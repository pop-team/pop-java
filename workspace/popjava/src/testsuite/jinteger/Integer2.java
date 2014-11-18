package testsuite.jinteger;

import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;
import popjava.base.Semantic;

@POPClass(classId = 1000, className = "Integer2", deconstructor = true)
public class Integer2 {
    
	private int data;
	
	public Integer2(){
	}
	
	@POPSyncMutex
	public void add(Jinteger i){
		data += i.get();
	}
	
	@POPAsyncSeq
	public void set(int value){
		data = value;
	}
	
	@POPSyncConc
	public int get(){
		return data;
	}
}
