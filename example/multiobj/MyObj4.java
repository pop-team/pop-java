import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;

@POPClass
public class MyObj4 {
	protected int data;
	
	public MyObj4(){
	}
	
	@POPSyncSeq
	public void set(int value) {
		data = value;
	}
	
	@POPSyncConc
	public int get(){
		return data + 1000;
	}
}

