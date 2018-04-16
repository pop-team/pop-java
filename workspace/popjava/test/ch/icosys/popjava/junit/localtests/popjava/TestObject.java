package ch.icosys.popjava.junit.localtests.popjava;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class TestObject extends POPObject{

	@POPSyncConc
	public int test(){
		return 1234;
	}
	
}
