package junit.localtests.jvmObject;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.annotation.POPConfig.Type;
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

	@POPObjectDescription(localJVM = true)
	public LocalObject(Long fake){
		value = (int)(Math.random() * 10000);
		reference = PopJava.newActive(LocalObject.class, "localhost");
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
			reference = PopJava.newActive(LocalObject.class, 4.0);
		}else{
			reference = PopJava.newActive(LocalObject.class, url);
		}
	}
}
