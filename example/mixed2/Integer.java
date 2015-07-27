import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPClass;
import popjava.annotation.POPParameter;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.util.LogWriter;

@POPClass(classId = 1000, deconstructor = true)
public class Integer {
	private int value;
	
	public Integer(){
	    LogWriter.writeDebugInfo("CREATED JAVA OBJECT");
		value = 0;
	}

    @POPSyncMutex
    public void add(Integer i){
        LogWriter.writeDebugInfo("Integer.add old value"+value);
        value += i.get();
        LogWriter.writeDebugInfo("Integer.add new value"+value);
    }

	@POPSyncConc
	public int get(){
		return value;
	}
	
	@POPAsyncSeq
	public void set(int val){
		value = val;
	}
}
