package ch.icosys.popjava.junit.localtests.subclasses;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class C extends POPObject implements D {

	@POPObjectDescription(url = "localhost")
	public C() {

	}

	@Override
	@POPSyncConc
	public B getTest() {
		return PopJava.newActive(this, B.class);
	}

}
