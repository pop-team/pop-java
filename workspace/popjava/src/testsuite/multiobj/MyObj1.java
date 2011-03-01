package testsuite.multiobj;

import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.base.Semantic;
import popjava.*;



public class MyObj1 extends POPObject {
	protected int data;
	
	public MyObj1(){
		setClassId(1208);
		Class<?> c = MyObj1.class;
		od.setHostname("localhost");
		initializePOPObject(c);
		addSemantic(c, "set",	Semantic.Sequence | Semantic.Synchronous);
		addSemantic(c, "get",	Semantic.Concurrent | Semantic.Synchronous);
	}
	
	public int get(){
		return data + 4;
	}
	
	public void set(int value) throws POPException {
		MyObj2 o2 = (MyObj2) PopJava.newActive(MyObj2.class);
		o2.set(value);
		data=o2.get();	
	}
	
}
