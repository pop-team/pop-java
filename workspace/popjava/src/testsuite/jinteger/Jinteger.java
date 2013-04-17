package testsuite.jinteger;

import popjava.base.POPObject;
import popjava.base.Semantic;

public class Jinteger extends POPObject {
	private int data;
	public Jinteger(){
		Class<?> c = Jinteger.class;
		//setClassName("Jinteger");
		setClassId(1001);
		hasDestructor(true);
		initializePOPObject();
		addSemantic(c, "set", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "get", Semantic.Synchronous | Semantic.Concurrent);
	}
	
	public void set(int value){
		data = value;
	}
	
	public int get(){
		return data;
	}
}
