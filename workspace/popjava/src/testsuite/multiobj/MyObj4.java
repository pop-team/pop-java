package testsuite.multiobj;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;

@POPClass(classId = 1211)
public class MyObj4{
	protected int data;
	
	@POPObjectDescription(url = "localhost")
	public MyObj4(){		
	}
	
	@POPSyncSeq
	public void set(int value) {
		data = value;
	}
	
	@POPSyncConc
	public int get(){
		return data + 1000;
	}
}
