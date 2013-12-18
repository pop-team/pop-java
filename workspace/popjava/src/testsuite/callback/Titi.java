package testsuite.callback;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.annotation.POPParameter.Direction;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;

@POPClass(classId = 1034)
public class Titi extends POPObject {
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
