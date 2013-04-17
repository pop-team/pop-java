package junit.annotations;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.base.POPObject;

@POPClass
public class MyAnnotatedPopObject2 extends POPObject{

	@POPObjectDescription(url="1111")
	public MyAnnotatedPopObject2(){
		initializePOPObject();
	}
	
	@POPObjectDescription(url="2222")
	public MyAnnotatedPopObject2(int test){
		initializePOPObject();
	}
}
