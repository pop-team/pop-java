package testsuite.popc_integer;

import popjava.base.POPObject;
import popjava.base.Semantic;

public class Jinteger extends POPObject {
	private int value;
	
	public Jinteger(){
		Class<?> c = Jinteger.class;
		//setClassName("Jinteger");
		setClassId(1001);
		od.setPower(100, 10);
		hasDestructor(true);
		initializePOPObject();
		addSemantic(c, "add", Semantic.Mutex | Semantic.Synchronous);
		addSemantic(c, "jadd", Semantic.Mutex | Semantic.Synchronous);
		addSemantic(c, "jget", Semantic.Concurrent | Semantic.Synchronous);
		addSemantic(c, "jset", Semantic.Sequence | Semantic.Asynchronous);
	}
	
	public int jget() {
		return value;
	}

	
	public void jset(int value) {
		this.value=value;
	}

	public void jadd(Jinteger other) {
		this.value += other.jget();
//		try {
//			Jinteger i = (Jinteger)PopJava.newActive(Jinteger.class, other);
//			int value = i.jget();
//			this.value+=value;
//		} catch (POPException e) {
//			e.printStackTrace();
//		}	
	}
	
	public void add(Integer other){
		this.value += other.get();
	}
}
