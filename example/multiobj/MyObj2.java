import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;


@POPClass
public class MyObj2 {
	protected int data;
	
	public MyObj2(){
	}
	
	@POPSyncSeq
	public void set(int value) {
       
   		MyObj3 o3 = new MyObj3();
		o3.set(value);
		data=o3.get();
	}
	
	@POPSyncConc
	public int get(){
		return data + 30;
	}
	
}

