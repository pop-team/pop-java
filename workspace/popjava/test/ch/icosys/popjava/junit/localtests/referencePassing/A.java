package ch.icosys.popjava.junit.localtests.referencePassing;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class A extends POPObject {

	@POPObjectDescription(url = "localhost")
	public A() {

	}

	@POPSyncConc
	public B getB() {
		return PopJava.newActive(this, B.class);
	}

}
