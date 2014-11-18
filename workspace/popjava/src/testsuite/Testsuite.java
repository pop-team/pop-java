package testsuite;

import popjava.util.LogWriter;
import testsuite.callback.CallBackMain;
import testsuite.integer.TestInteger;

public class Testsuite {

	public static void main(String ... args){
		LogWriter.writeDebugInfo("Start callback");
		CallBackMain.main();
		TestInteger.main();
		
	}
	
}
