package ch.icosys.popjava.junit.localtests.delayedCreation;

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
	public int getTestValue() {
		return 1234;
	}

}
