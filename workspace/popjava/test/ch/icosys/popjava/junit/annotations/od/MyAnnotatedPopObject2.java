package ch.icosys.popjava.junit.annotations.od;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class MyAnnotatedPopObject2 extends POPObject {

	@POPObjectDescription(url = "1111")
	public MyAnnotatedPopObject2() {
		initializePOPObject();
	}

	@POPObjectDescription(url = "2222")
	public MyAnnotatedPopObject2(int test) {
		initializePOPObject();
	}
}
