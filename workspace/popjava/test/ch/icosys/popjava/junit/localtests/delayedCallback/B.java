package ch.icosys.popjava.junit.localtests.delayedCallback;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

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
