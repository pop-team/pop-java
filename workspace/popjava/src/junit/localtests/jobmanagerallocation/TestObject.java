package junit.localtests.jobmanagerallocation;

import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class TestObject extends POPObject{

	public TestObject(){
	}
	
	@POPSyncConc
	public int getValue(){
		return 1234;
	}
	
}
