package testsuite.multiobj;

import popjava.base.POPObject;
import popjava.base.Semantic;


public class MyObj4 extends POPObject{
	protected int data;
	
	public MyObj4(){
		setClassId(1211);
		Class<?> c = MyObj4.class;
		od.setHostname("localhost");
		initializePOPObject();
		addSemantic(c, "set",	Semantic.Sequence | Semantic.Synchronous);
		addSemantic(c, "get",	Semantic.Concurrent | Semantic.Synchronous);
		
	}
	
	public void set(int value) {
		data = value;
	}
	
	public int get(){
		return data + 1000;
	}
}
