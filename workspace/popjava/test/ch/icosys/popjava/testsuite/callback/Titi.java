package ch.icosys.popjava.testsuite.callback;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.POPObject;

@POPClass(classId = 1034)
public class Titi extends POPObject{
	private int identity;
	
	@POPObjectDescription(url = "localhost")
	public Titi(){
		identity = -1;
	}
	
	@POPSyncSeq
	public void setIdent(int i){
		identity = i;
	}
	
	@POPSyncSeq
	public void computeIdent(Toto t){
		t.setIdent(identity);
	}
}
