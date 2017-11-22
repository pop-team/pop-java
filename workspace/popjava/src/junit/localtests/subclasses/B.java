package junit.localtests.subclasses;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class B extends POPObject implements A{

	@POPObjectDescription(url = "localhost")
	public B(){
		
	}
	
	@Override
	@POPSyncConc
	public String a() {
		return "asdf";
	}

	
	
}
