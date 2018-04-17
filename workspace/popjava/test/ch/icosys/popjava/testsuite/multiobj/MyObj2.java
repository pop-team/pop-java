package ch.icosys.popjava.testsuite.multiobj;

import ch.icosys.popjava.core.*;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.POPException;

@POPClass(classId = 1209)
public class MyObj2 {
	protected int data;

	@POPObjectDescription(url = "localhost")
	public MyObj2() {
	}

	@POPSyncSeq
	public void set(int value) throws POPException {
		MyObj3 o3 = (MyObj3) PopJava.newActive(this, MyObj3.class);
		o3.set(value);
		data = o3.get();
	}

	@POPSyncConc
	public int get() {
		return data + 30;
	}

}
