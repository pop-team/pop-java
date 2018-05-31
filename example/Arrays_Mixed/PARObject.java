import ch.icosys.popjava.core.annotation.POPClass;
import  ch.icosys.popjava.core.annotation.POPSyncSeq;

@POPClass
public class PARObject {
	
	public PARObject(){
	}
	
	@POPSyncSeq
	public void sendChar(int length, char[] tab, int length2, char [] tab2){
	}
}

