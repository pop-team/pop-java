package testsuite.integer;

import popjava.PopJava;
import popjava.base.*;

public class Integer extends POPObject{
	protected int value;
	
	public Integer(){
		Class<?> c = Integer.class;
		initializePOPObject(c);
		addSemantic(c, "get", Semantic.Synchronous | Semantic.Mutex);
		addSemantic(c, "add", Semantic.Synchronous | Semantic.Mutex);
		addSemantic(c, "set", Semantic.Asynchronous | Semantic.Sequence);		
	}
	
	public int get(){
		return this.value;
	}
	
	public void add ( Integer i) 	throws POPException {
		i=(Integer)PopJava.newActive(Integer.class, i.getAccessPoint());
		 value += i.get();
	}
	
	
	public void set(int val){
		this.value=val;
	}
}
