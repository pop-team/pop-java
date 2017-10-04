package junit.localtests.serializable;

import java.util.ArrayList;
import java.util.List;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;

/**
 *
 * @author Davide Mazzoleni
 */
@POPClass
public class Obj extends POPObject {
	
	public static final MySerializable A = new MySerializable("tttm", 9876, 1.7844e12);
	public static final MySerializable B = new MySerializable("hgdd", 1294, 1.9888e56);
	public static final List<MySerializable> C = new ArrayList<>();
	public static final ArrayList<MySerializable> D = new ArrayList<>();
	static {
		C.add(A);
		C.add(B);
		D.addAll(C);
	}

	@POPObjectDescription(url = "localhost")
	public Obj() {
	}
	
	@POPSyncSeq
	public boolean isAEquals(MySerializable a) {
		return A.equals(a);
	}
	
	@POPSyncSeq
	public MySerializable getB() {
		return B;
	}
	
	@POPSyncSeq
	public boolean areStructEquals(List<MySerializable> c) {
		return C.equals(c);
	}
	
	@POPSyncSeq
	public List<MySerializable> getStruct() {
		return C;
	}
	
	@POPSyncSeq
	public boolean areSerializableStructEquals(ArrayList<MySerializable> d) {
		return D.equals(d);
	}
	
	@POPSyncSeq
	public ArrayList<MySerializable> getSerializableStruct() {
		return D;
	}
}
