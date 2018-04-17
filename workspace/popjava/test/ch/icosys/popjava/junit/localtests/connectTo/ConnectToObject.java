package ch.icosys.popjava.junit.localtests.connectTo;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPConfig.Type;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class ConnectToObject extends POPObject {

	private String message;

	public ConnectToObject() {

	}

	public ConnectToObject(String message) {
		this.message = message;
	}

	public ConnectToObject(@POPConfig(Type.ACCESS_POINT) String accessPoint, String message) {
		this(message);
	}

	@POPSyncConc
	public String getMessage() {
		return message;
	}

}
