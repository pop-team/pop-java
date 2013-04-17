package testsuite.callback;

import popjava.*;
import popjava.base.*;

public class Toto extends POPObject {
	private int identity;
	Toto thisObject;
	public Toto(){
		setClassId(1035);
		od.setHostname("localhost");
		Class<?> c = Toto.class;
		initializePOPObject();
		addSemantic(c, "setIdent", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "getIdent", Semantic.Synchronous | Semantic.Concurrent);
		thisObject = this;
	}
	
	public void setIdent(int i){
		identity = i;
	}
	
	public int getIdent() throws POPException {
		Titi t = (Titi)PopJava.newActive(Titi.class);
		setIdent(222);
		t.computeIdent((Toto)PopJava.newActive(Toto.class, thisObject.getAccessPoint()));
		return identity;
	}
}
