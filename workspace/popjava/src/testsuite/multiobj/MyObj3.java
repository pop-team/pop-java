package testsuite.multiobj;

import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.base.Semantic;
import popjava.*;



public class MyObj3 extends POPObject{
	protected int data;
	
	public MyObj3(){
		setClassId(1210);
		Class<?> c = MyObj3.class;
		od.setHostname("localhost");
		initializePOPObject();
		addSemantic(c, "set",	Semantic.Sequence | Semantic.Synchronous);
		addSemantic(c, "get",	Semantic.Concurrent | Semantic.Synchronous);
		
	}
	
	public void set(int value) throws POPException {
		MyObj4 o4 = (MyObj4) PopJava.newActive(MyObj4.class);
		o4.set(value);
		data=o4.get();
		
	}
	
	public int get(){
		return data + 200;
	}
	
}
