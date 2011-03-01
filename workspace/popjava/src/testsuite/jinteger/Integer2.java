package testsuite.jinteger;

import popjava.base.POPObject;
import popjava.base.Semantic;

public class Integer2 extends POPObject {
	private int data;
	
	public Integer2(){
		Class<?> c = Integer2.class;
		setClassName("Integer2");
		setClassId(1000);
		hasDestructor(true);
		initializePOPObject(c);
		addSemantic(c, "set", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "get", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "add", Semantic.Synchronous | Semantic.Mutex);
	}
	
	public void add(Jinteger i){
		data += i.get();
	}
	
	public void set(int value){
		data = value;
	}
	
	public int get(){
		return data;
	}
}
