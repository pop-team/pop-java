package ch.icosys.popjava.junit.localtests.jobmanagerallocation;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class TestObject extends POPObject{

	public TestObject(){
	}
	
	@POPSyncConc
	public int getValue(){
		return 1234;
	}
	
}
