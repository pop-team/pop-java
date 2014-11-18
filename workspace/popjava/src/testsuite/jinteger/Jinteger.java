package testsuite.jinteger;

import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;
import popjava.base.Semantic;

@POPClass(classId = 1001, deconstructor = true)
public class Jinteger{
    
	private int data;
	
	public Jinteger(){
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
