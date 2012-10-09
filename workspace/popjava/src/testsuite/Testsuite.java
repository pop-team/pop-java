package testsuite;

import popjava.util.LogWriter;
import testsuite.callback.CallBackMain;

public class Testsuite {

	public static void main(String ... args){
		LogWriter.writeDebugInfo("Start callback");
		CallBackMain.main();
	}
	
}
