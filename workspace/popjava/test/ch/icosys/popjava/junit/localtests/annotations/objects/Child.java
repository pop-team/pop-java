package ch.icosys.popjava.junit.localtests.annotations.objects;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncConc;

@POPClass(classId = 1235)
public class Child extends Parent {

	public Child() {
	}

	@POPSyncConc(id = 20)
	public void childTest() {

	}

	@Override
	@POPSyncConc(id = 22)
	public void test2() {

	}
}
