package ch.icosys.popjava.junit.benchmarks.objects;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class MyObject extends POPObject{
	
	@POPObjectDescription(localJVM = true, protocols = "ssl")
	public MyObject() {
		
	}
	
	@POPSyncConc
	public void test() {
		
	}
	
}
