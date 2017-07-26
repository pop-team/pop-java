package junit.localtests.creation;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;

/**
 *
 * @author dosky
 */
@POPClass
public class ObjA {

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

	public int getVal() {
		return val;
	}

	public int getbVal() {
		return bVal;
	}

	public ObjB getB() {
		return b;
	}
}
