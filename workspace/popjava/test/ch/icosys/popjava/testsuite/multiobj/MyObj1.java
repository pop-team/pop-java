package ch.icosys.popjava.testsuite.multiobj;

import ch.icosys.popjava.core.*;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.POPException;

@POPClass(classId = 1208)
public class MyObj1 {
	protected int data;

	@POPObjectDescription(url = "localhost")
	public MyObj1() {
	}

	@POPSyncConc
	public int get() {
		return data + 4;
	}

	@POPSyncSeq
	public void set(int value) throws POPException {
		MyObj2 o2 = (MyObj2) PopJava.newActive(this, MyObj2.class);
		o2.set(value);
		data = o2.get();
	}

}
