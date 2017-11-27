package junit.localtests.creation;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;

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
		b = PopJava.newActive(ObjB.class, this, bVal);
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
