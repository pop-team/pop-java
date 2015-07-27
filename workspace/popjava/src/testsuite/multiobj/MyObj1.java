package testsuite.multiobj;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPException;
import popjava.*;

@POPClass(classId = 1208)
public class MyObj1{
	protected int data;
	
	@POPObjectDescription(url = "localhost")
	public MyObj1(){
	}
	
	@POPSyncConc
	public int get(){
		return data + 4;
	}
	
	@POPSyncSeq
	public void set(int value) throws POPException {
		MyObj2 o2 = (MyObj2) PopJava.newActive(MyObj2.class);
		o2.set(value);
		data=o2.get();	
	}
	
}
