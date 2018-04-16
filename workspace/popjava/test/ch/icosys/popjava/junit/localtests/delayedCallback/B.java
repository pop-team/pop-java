package junit.localtests.delayedCallback;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class B extends POPObject{

	private A a;
	
	@POPObjectDescription(url = "localhost")
	public B(){
	}
	
	@POPObjectDescription(url = "localhost")
	public B(A a){
		this.a = a.makePermanent();
	}
	
	@POPSyncConc
	public void work(){
		a.setValue(1234);
	}
}
