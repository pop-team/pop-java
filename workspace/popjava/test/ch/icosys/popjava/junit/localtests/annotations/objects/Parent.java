package ch.icosys.popjava.junit.localtests.annotations.objects;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPPrivate;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

@POPClass(classId = 1234)
public class Parent extends POPObject {

	public Parent() {

	}

	@POPSyncConc(id = 20)
	public void parentTest() {

	}

	@POPSyncConc(id = 21)
	public void test2() {

	}

	@POPPrivate
	public int testPrivate() {
		return 1234;
	}
}
