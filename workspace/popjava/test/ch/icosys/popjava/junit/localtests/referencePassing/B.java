package junit.localtests.referencePassing;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class B extends POPObject{

	@POPObjectDescription(url = "localhost")
	public B(){
		
	}
	
	@POPSyncConc
	public String value(){
		return "asdf";
	}
	
}
