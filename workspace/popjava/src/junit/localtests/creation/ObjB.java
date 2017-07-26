package junit.localtests.creation;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;

/**
 *
 * @author dosky
 */
@POPClass
public class ObjB {

	private ObjA a;
	private int val;
	
	public ObjB() {
	}

	@POPObjectDescription(url = "localhost")
	ObjB(ObjA a, int bVal) {
		this.a = a;
		val = bVal;
	}

	public ObjA getA() {
		return a;
	}

	public int getVal() {
		return val;
	}
}
