package junit.localtests.connectTo;

import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class ConnectToObject extends POPObject{

	private String message;
	
	public ConnectToObject(){
		
	}

	public ConnectToObject(String message){
		this.message = message;
	}
	
	public ConnectToObject(@POPConfig(Type.ACCESS_POINT)String accessPoint, String message){
		this(message);
	}
	
	@POPSyncConc
	public String getMessage(){
		return message;
	}
	
}
