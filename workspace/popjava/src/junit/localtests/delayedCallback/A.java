package junit.localtests.delayedCallback;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.base.POPObject;

@POPClass
public class A extends POPObject{

	private int value;
	
	@POPObjectDescription(url = "localhost")
	public A(){
	}
	
	public void test(){
		B b = PopJava.newActive(B.class, this);
		b.work();
	}
	
	public void setValue(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
	
}
