package junit.localtests.delayedCallback;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.base.POPObject;

@POPClass
public class B extends POPObject{

	private A a;
	
	public B(){
		
	}
	
	@POPObjectDescription(url = "localhost")
	public B(A a){
		this.a = (A)a.makePermanent();
	}
	
	public void work(){
		a.setValue(1234);
	}
}
