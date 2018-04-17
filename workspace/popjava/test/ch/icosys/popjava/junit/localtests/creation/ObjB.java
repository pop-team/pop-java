package ch.icosys.popjava.junit.localtests.creation;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.POPObject;

/**
 *
 * @author dosky
 */
@POPClass
public class ObjB extends POPObject {

	private ObjA a;

	private int val;

	public ObjB() {
	}

	@POPObjectDescription(url = "localhost")
	public ObjB(ObjA a, int bVal) {
		this.a = a;
		val = bVal;
	}

	@POPSyncSeq
	public ObjA getA() {
		return a;
	}

	@POPSyncSeq
	public int getVal() {
		return val;
	}
}
