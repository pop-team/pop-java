package ch.icosys.popjava.testsuite.callback;

import ch.icosys.popjava.core.*;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.*;

@POPClass(classId = 1035)
public class Toto extends POPObject {
	private int identity;

	@POPObjectDescription(url = "localhost")
	public Toto() {
	}

	@POPSyncSeq
	public void setIdent(int i) {
		identity = i;
	}

	@POPSyncConc
	public int getIdent() throws POPException {
		Titi t = PopJava.newActive(null, Titi.class);
		setIdent(222);
		t.computeIdent(this);
		return identity;
	}
}
