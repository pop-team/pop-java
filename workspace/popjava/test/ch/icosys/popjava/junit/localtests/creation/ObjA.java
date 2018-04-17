package ch.icosys.popjava.junit.localtests.creation;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.POPObject;

/**
 *
 * @author dosky
 */
@POPClass
public class ObjA extends POPObject {

	private int val;

	private ObjB b;

	private int bVal;

	public ObjA() {
	}

	@POPObjectDescription(url = "localhost")
	public ObjA(int val) {
		this.val = val;
		bVal = (int) (Math.random() * 100);
		b = PopJava.newActive(this, ObjB.class, this, bVal);
	}

	@POPSyncSeq
	public int getVal() {
		return val;
	}

	@POPSyncSeq
	public int getbVal() {
		return bVal;
	}

	@POPSyncSeq
	public ObjB getB() {
		return b;
	}
}
