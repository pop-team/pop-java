package ch.icosys.popjava.testsuite.multiobj;

import ch.icosys.popjava.core.*;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.POPException;

@POPClass(classId = 1210)
public class MyObj3 {
	protected int data;

	@POPObjectDescription(url = "localhost")
	public MyObj3() {
	}

	@POPSyncSeq
	public void set(int value) throws POPException {
		MyObj4 o4 = (MyObj4) PopJava.newActive(this, MyObj4.class);
		o4.set(value);
		data = o4.get();

	}

	@POPSyncConc
	public int get() {
		return data + 200;
	}

}
