package junit.localtests.annotations.objects;

import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;

@POPClass
public class ConcreteChild extends AbstractChild{

	public String testNonAbstract(){
		return "B";
	}
	
	@Override
	public String getStuff() {
		return "My String";
	}
	
	@POPSyncConc
	@Override
	public String getStuff2() {
		return "My String2";
	}

}
