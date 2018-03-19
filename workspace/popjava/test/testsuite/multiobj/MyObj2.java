package testsuite.multiobj;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPException;
import popjava.*;

@POPClass(classId = 1209)
public class MyObj2{
	protected int data;
	
	@POPObjectDescription(url = "localhost")
	public MyObj2(){		
	}
	
	@POPSyncSeq
	public void set(int value) throws POPException {
		MyObj3 o3 = (MyObj3) PopJava.newActive(this, MyObj3.class);
		o3.set(value);
		data=o3.get();
	}
	
	@POPSyncConc
	public int get(){
		return data + 30;
	}
	
}
