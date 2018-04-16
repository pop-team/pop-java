package ch.icosys.popjava.testsuite.pure.callback;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.*;

@POPClass(classId = 1035)
public class Toto{
    
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
