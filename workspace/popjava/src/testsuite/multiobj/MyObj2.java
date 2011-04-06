package testsuite.multiobj;

import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.base.Semantic;
import popjava.*;



public class MyObj2 extends POPObject{
	protected int data;
	
	public MyObj2(){
		setClassId(1209);
		Class<?> c = MyObj2.class;
		od.setHostname("localhost");
		initializePOPObject(c);
		addSemantic(c, "set",	Semantic.Sequence | Semantic.Synchronous);
		addSemantic(c, "get",	Semantic.Concurrent | Semantic.Synchronous);
		
	}
	
	public void set(int value) throws POPException {
		MyObj3 o3 = (MyObj3) PopJava.newActive(MyObj3.class);
		o3.set(value);
		data=o3.get();
	}
	
	public int get(){
		return data + 30;
	}
	
}
