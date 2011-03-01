package testsuite.chartest;

import popjava.base.POPObject;
import popjava.base.Semantic;

public class PARObject extends POPObject {
	
	public PARObject(){
		setClassId(1501);
		setClassName("PARObject");
		Class<?> c = PARObject.class;
		hasDestructor(true);
		initializePOPObject(c);
		addSemantic(c, "sendChar", Semantic.Sequence | Semantic.Synchronous);
	}
	
	public void sendChar(int length, char[] tab){
		tab[1] = 'd';		
	}
}
