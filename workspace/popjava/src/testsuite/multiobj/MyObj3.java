package testsuite.multiobj;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPException;
import popjava.*;

@POPClass(classId = 1210)
public class MyObj3{
	protected int data;
	
	@POPObjectDescription(url = "localhost")
	public MyObj3(){		
	}
	
	@POPSyncSeq
	public void set(int value) throws POPException {
		MyObj4 o4 = (MyObj4) PopJava.newActive(MyObj4.class);
		o4.set(value);
		data=o4.get();
		
	}
	
	@POPSyncConc
	public int get(){
		return data + 200;
	}
	
}
