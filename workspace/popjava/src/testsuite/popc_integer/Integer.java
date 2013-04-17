package testsuite.popc_integer;


import popjava.base.POPObject;
import popjava.base.Semantic;

public class Integer extends POPObject {
	private int value=0;
	
	
	public Integer() {
		setClassId(1000);
		setClassName("Integer");
		Class<?> c = Integer.class;
		hasDestructor(true);
		initializePOPObject();
		addSemantic(c, "get", Semantic.Concurrent | Semantic.Synchronous);
		addSemantic(c, "set", Semantic.Sequence | Semantic.Asynchronous);
		addSemantic(c, "add", Semantic.Mutex | Semantic.Synchronous);
	}
	
	
	public int get() {
		return value;
	}

	
	public void set(int value) {
		this.value=value;
	}

	public void add(Integer myInteger) {
		this.value += myInteger.get();
	}
}
