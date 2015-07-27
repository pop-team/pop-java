package test;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;
import popjava.baseobject.ConnectionType;
import popjava.util.LogWriter;

@POPClass
public class Titi {
private int identity;
   private Toto toto;
   
   @POPObjectDescription(url="localhost")
	public Titi(){
		identity = -1;
	}
	
	
   @POPSyncSeq
	public void setIdent(int i){
		identity = i;
	}

   @POPSyncSeq
   public void setToto(Toto t){
		LogWriter.writeDebugInfo("*****"+PopJava.getAccessPoint(t));
		toto = t;
   }
   
   @POPSyncSeq
	public void computeIdent(){
		toto.setIdent(identity);
	}
}

