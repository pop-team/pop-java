import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;

@POPClass
public class MyObj1 {
	protected int data;

	public MyObj1() {
	}

	@POPSyncSeq
	public int get() {
		return data + 4;
	}

	@POPSyncConc
	public void set(int value) {

		MyObj2 o2 = new MyObj2();
		o2.set(value);
		data = o2.get();

	}

}
