package testsuite.method;

//Import added by the POP-Java compiler
import popjava.base.POPObject;
import popjava.base.Semantic;

public class MethodObj extends POPObject {
	private int value;

	public MethodObj() {
		Class<?> c = MethodObj.class;
		initializePOPObject(c);
		od.setSearch(10, 10, 0);
		addSemantic(c, "get", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setSeq", Semantic.Asynchronous | Semantic.Sequence);
		value = 0;
	}

	public int get() {
		return value;
	}

	public void setSeq(int val) {
		try {
			Thread.sleep(1000);
			value = val;
		} catch (InterruptedException e) {
		}
	}
}