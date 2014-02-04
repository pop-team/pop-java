package junit.localtests.delayedCreation;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class A extends POPObject{
	
	@POPObjectDescription(url = "localhost")
	public A(){
	}
	
	@POPSyncConc
	public int getTestValue(){
		return 1234;
	}

}
