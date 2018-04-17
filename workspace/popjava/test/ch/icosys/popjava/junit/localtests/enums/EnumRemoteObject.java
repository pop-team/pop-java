package ch.icosys.popjava.junit.localtests.enums;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncMutex;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class EnumRemoteObject extends POPObject {

	public enum Test {
		A, B, C
	}

	private Test constructor;

	private Test method;

	public EnumRemoteObject() {

	}

	@POPObjectDescription(url = "localhost")
	public EnumRemoteObject(Test param) {
		constructor = param;
		System.out.println("Const " + param.name());
	}

	@POPSyncMutex
	public void setEnum(Test param) {
		method = param;
		System.out.println("Func " + param.name());
	}

	@POPSyncMutex
	public Test getConstructor() {
		return constructor;
	}

	@POPSyncMutex
	public Test getMethod() {
		return method;
	}
}
