package ch.icosys.popjava.junit.localtests.annotations.objects;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncConc;

@POPClass
public abstract class AbstractChild extends Parent{

	@POPSyncConc 
	public String testNonAbstract(){
		return "A";
	}
	
	@POPSyncConc
	public String nonInheritedTest(){
		return "C";
	}
	
	@POPSyncConc
	public abstract String getStuff();
	
	@POPSyncConc
	public abstract String getStuff2();
	
}
