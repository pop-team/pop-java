package testsuite.callback;

import popjava.base.POPObject;
import popjava.base.Semantic;

public class Titi extends POPObject {
	private int identity;
	
	public Titi(){
		setClassId(1034);
		od.setHostname("localhost");
		Class<?> c = Titi.class;
		initializePOPObject();
		identity = -1;
		addSemantic(c, "setIdent", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "computeIdent", Semantic.Synchronous | Semantic.Sequence);
	}
	
	public void setIdent(int i){
		identity = i;
	}
	
	public void computeIdent(Toto t){
		t.setIdent(identity);
	}
}
