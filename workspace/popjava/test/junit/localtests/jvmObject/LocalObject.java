package junit.localtests.jvmObject;

import java.util.concurrent.TimeUnit;
import popjava.PopJava;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPSyncConc;
import popjava.base.POPException;
import popjava.base.POPObject;

@POPClass
public class LocalObject extends POPObject{

	int value;
	private LocalObject reference;
	
	public LocalObject(){
		value = -1;
	}
	
	@POPObjectDescription(localJVM = true)
	public LocalObject(Double fake){
		value = (int)(Math.random() * 10000);
	}
	
	public LocalObject(@POPConfig(Type.URL)String url){
		value = (int)(Math.random() * 10000);
	}
	
	public LocalObject(@POPConfig(Type.LOCAL_JVM) boolean useLocalVM, @POPConfig(Type.URL)String url){
		value = (int)(Math.random() * 10000);
	}
	
	public LocalObject(@POPConfig(Type.LOCAL_JVM) boolean useLocalVM, @POPConfig(Type.URL)String url, String test){
		value = (int)(Math.random() * 10000);
	}
	
	@POPObjectDescription(localJVM = true, protocols = { "socket:14000", "socket" })
	public LocalObject(boolean localDouble) {
		value = 500;
	}

	@POPObjectDescription(localJVM = true)
	public LocalObject(Long fake){
		value = (int)(Math.random() * 10000);
		reference = PopJava.newActive(this, LocalObject.class, "localhost");
		reference.setReference(this);
	}
	
	@POPSyncSeq
	public int getValue(){
		return value;
	}
	
	@POPSyncSeq
	public void setReference(LocalObject reference){
		this.reference = reference;
	}
	
	@POPSyncSeq
	public int getRefernceValue(){
		return reference.getValue();
	}
	
	@POPSyncSeq
	public LocalObject getReference(){
		return reference;
	}

	@POPSyncSeq
	public void createReference(String url, boolean local){
		if(local){
			reference = PopJava.newActive(this, LocalObject.class, 4.0);
		}else{
			reference = PopJava.newActive(this, LocalObject.class, url);
		}
	}
	
	private int asyncVal;
	private boolean report;
	@POPSyncConc
	public void setAsyncVal(int n) {
		asyncVal = n;
	}
	
	@POPSyncConc
	public int getAsyncVal() {
		return asyncVal;
	}
	
	@POPAsyncConc
	public void asyncCheck(int m) throws POPException, Exception {
		TimeUnit.MILLISECONDS.sleep(500);
		org.junit.Assert.assertEquals(m, asyncVal);
		report = m == asyncVal;
	}
	
	@POPSyncSeq
	public boolean getAsyncReport() {
		System.out.println(asyncVal);
		return report;
	}
}
