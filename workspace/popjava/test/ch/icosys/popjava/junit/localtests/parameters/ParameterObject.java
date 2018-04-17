package ch.icosys.popjava.junit.localtests.parameters;

import java.util.ArrayList;
import java.util.List;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPAsyncMutex;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncMutex;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class ParameterObject extends POPObject implements MyInterface {

	private String temp;

	@POPSyncConc
	public void noParam() {

	}

	@POPSyncConc
	public int simple(int a) {
		return a;
	}

	@POPSyncConc
	public void impossibleParam(List<String> test) {

	}

	@POPAsyncMutex
	public void setValue(String string) {
		temp = string;
	}

	@POPSyncMutex
	public String getValue() {
		return temp;
	}

	@POPSyncConc
	public List<String> impossibleReturn() {
		return new ArrayList<>();
	}

	@POPSyncConc
	public void testInterfaceErrorParameter(MyInterface obj) {

	}

	@POPSyncConc
	public MyInterface testInterfaceErrorReturn() {
		return PopJava.getThis(this);
	}
}
