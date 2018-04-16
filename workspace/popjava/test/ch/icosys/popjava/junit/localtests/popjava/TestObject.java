package junit.localtests.popjava;

import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class TestObject extends POPObject{

	@POPSyncConc
	public int test(){
		return 1234;
	}
	
}
