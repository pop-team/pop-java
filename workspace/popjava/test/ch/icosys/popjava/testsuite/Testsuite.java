package ch.icosys.popjava.testsuite;

import ch.icosys.popjava.core.util.LogWriter;
import ch.icosys.popjava.testsuite.callback.CallBackMain;
import ch.icosys.popjava.testsuite.integer.TestInteger;

public class Testsuite {

	public static void main(String... args) {
		LogWriter.writeDebugInfo("Start callback");
		CallBackMain.main();
		TestInteger.main();

	}

}
