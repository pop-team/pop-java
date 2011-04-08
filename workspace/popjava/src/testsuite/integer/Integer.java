package testsuite.integer;

import popjava.base.*;

public class Integer extends POPObject{
	protected int value;
	
	public Integer(){
		Class<?> c = Integer.class;
		od.setHostname("localhost");
		initializePOPObject(c);
		addSemantic(c, "get", Semantic.Synchronous | Semantic.Mutex);
		addSemantic(c, "add", Semantic.Synchronous | Semantic.Mutex);
		addSemantic(c, "set", Semantic.Asynchronous | Semantic.Sequence);
		value = 10;
	}
	

	
	public int get(){
		return this.value;
	}
	
	public void add(Integer i){
		int val = i.get();
		this.value+=val;
	}
	
	
	public void set(int val){
		this.value=val;
	}
}
