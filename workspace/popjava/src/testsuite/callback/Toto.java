package testsuite.callback;

import popjava.*;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
import popjava.base.*;

@POPClass(classId = 1035)
public class Toto extends POPObject {
	private int identity;
	Toto thisObject;
	
	@POPObjectDescription(url = "localhost")
	public Toto(){
		thisObject = this;
	}
	
	@POPSyncSeq
	public void setIdent(int i){
		identity = i;
	}
	
	@POPSyncConc
	public int getIdent() throws POPException {
		Titi t = PopJava.newActive(Titi.class);
		setIdent(222);
		t.computeIdent(thisObject);
		return identity;
	}
}
