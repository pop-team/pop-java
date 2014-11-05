package testsuite.pure.callback;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
import popjava.base.*;

@POPClass(classId = 1035)
public class Toto extends POPObject {
    
	private int identity;
	
	@POPObjectDescription(url = "localhost")
	public Toto(){
	}
	
	@POPSyncSeq
	public void setIdent(int i){
		identity = i;

        System.out.println("C");
	}
	
	@POPSyncConc
	public int getIdent() throws POPException {
		Titi t = new Titi();
		setIdent(222);
		t.setIdent(this);
		
		for(int i = 0; i < 1; i++){
		    t.computeIdent();
		}

        System.out.println("D");
		return identity;
	}
}
