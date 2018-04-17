package ch.icosys.popjava.junit.localtests.arrays;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class ArrayObject extends POPObject {

	@POPObjectDescription(url = "localhost")
	public ArrayObject() {

	}

	@POPSyncConc
	public void testArray(byte[] array) {

	}

	@POPSyncConc
	public String[][] strings2d() {
		return new String[][] { { "a", "b" }, { "c" } };
	}

	@POPSyncConc
	public String[][] empty2d() {
		return new String[0][0];
	}

	@POPSyncConc
	public String[] empty1d() {
		return new String[0];
	}
}
