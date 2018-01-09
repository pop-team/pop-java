package junit.localtests.enums;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;

@POPClass
public class EnumRemoteObject extends POPObject{

	public enum Test{
		A,
		B,
		C
	}

	private Test constructor;
	private Test method;
	
	public EnumRemoteObject(){
		
	}
	
	@POPObjectDescription(url = "localhost")
	public EnumRemoteObject(Test param){
		constructor = param;
		System.out.println("Const "+param.name());
	}
	
	@POPSyncMutex
	public void setEnum(Test param){
		method = param;
		System.out.println("Func "+param.name());
	}
	
	@POPSyncMutex
	public Test getConstructor(){
		return constructor;
	}
	
	@POPSyncMutex
	public Test getMethod(){
		return method;
	}
}
