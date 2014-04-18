package junit.benchmarks.methods;

import java.io.IOException;
import java.rmi.Remote;

public interface RMIInterface extends  Remote {

	void noParamNoReturn() throws IOException;
	int noParamSimple() throws IOException;
	String[] noParamComplex() throws IOException;
	
	void simpleParam(int param) throws IOException;
	void complexParam(String [] param) throws IOException;
}
