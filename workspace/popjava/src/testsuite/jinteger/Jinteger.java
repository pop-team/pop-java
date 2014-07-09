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
		addSemantic(c, "set", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "get", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
	}
	
	public void set(int value){
		data = value;
	}
	
	public int get(){
		return data;
	}
}
