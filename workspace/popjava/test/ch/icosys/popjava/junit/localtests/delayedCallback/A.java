package ch.icosys.popjava.junit.localtests.delayedCallback;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncMutex;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class A extends POPObject{

	private int value;
	
	@POPObjectDescription(url = "localhost")
	public A(){
	}
	
	@POPSyncConc
	public void test(){
		B b = PopJava.newActive(this, B.class, this);
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
