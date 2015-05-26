import popjava.annotation.POPClass;
import popjava.annotation.POPSyncSeq;

@POPClass(classId = 1501)
public class PARObject {
	
	public PARObject(){
	}
	
	@POPSyncSeq
	public void sendChar(int length, char[] tab){
	}
}

