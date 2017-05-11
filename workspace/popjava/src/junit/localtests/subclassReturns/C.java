package junit.localtests.subclassReturns;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class C extends POPObject implements D{

	@POPObjectDescription(url = "localhost")
	public C(){
		
	}
	
	@Override
	@POPSyncConc
	public B getTest() {
		return PopJava.newActive(B.class);
	}

}
