package junit.localtests.referencePassing;

import popjava.PopJava;
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
	public B getB(){
		return PopJava.newActive(B.class);
	}
	
	
}
