package junit.localtests.creation;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;

/**
 *
 * @author dosky
 */
@POPClass
public class ObjB extends POPObject{

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
