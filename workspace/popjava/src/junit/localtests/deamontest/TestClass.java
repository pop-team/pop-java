package junit.localtests.deamontest;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;
import popjava.baseobject.ConnectionType;

@POPClass
public class TestClass extends POPObject {

	@POPObjectDescription(connection=ConnectionType.DEAMON, url = "localhost")
	public TestClass(){
	}
	
	@POPSyncConc
	public int test(){
		return 1234;
	}
	
}
