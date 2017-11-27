package junit.localtests.delayedCallback;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;

@POPClass
public class A extends POPObject{

	private int value;
	
	@POPObjectDescription(url = "localhost")
	public A(){
	}
	
	@POPSyncConc
	public void test(){
		B b = PopJava.newActive(B.class, this);
		b.work();
		b.exit();
	}
	
	@POPSyncMutex
	public void setValue(int value){
		this.value = value;
	}
	
	@POPSyncConc
	public int getValue(){
		return value;
	}
	
}
