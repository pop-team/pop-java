import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;

@POPClass
public class MyObj3 {
	protected int data;
	
	public MyObj3(){		
	}
	
	@POPSyncSeq
	public void set(int value) { 
      MyObj4 o4 = new MyObj4();
		o4.set(value);
		data=o4.get();
	}
	
	@POPSyncConc
	public int get(){
		return data + 200;
	}
	
}

