package junit.benchmarks.methods;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

import popjava.PopJava;
import popjava.system.POPSystem;

@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "benchmark-methods")
@BenchmarkHistoryChart(filePrefix = "benchmark-methods-history", maxRuns=10)
public class TestMethods extends AbstractBenchmark {

	private static final int REPETITIONS = 7000;
	private static POPMethods object;
	private static RMIInterface obj;
	private static RMIMethodsObj m;
	
	private static Thread server;
	private static SocketConnector con;
	
	private static final String [] COMPLEX_PARAM = new String[]{"aaasdff", "adsfasdf", "asdfffd"};
	
	@BeforeClass
	public static void startPOPJava() throws NotBoundException, UnknownHostException, IOException{
		POPSystem.initialize();
		object = PopJava.newActive(POPMethods.class);
		
		m = new RMIMethodsObj();
		m.publish();
		
		obj = RMIMethodsObj.getRemote();
		
		server = new Thread(new SocketServer());
		server.start();
		
		con = new SocketConnector();
	}
	
	@AfterClass
	public static void endPOPJava() throws NotBoundException, InterruptedException, IOException{
		con.close();
		POPSystem.end();
		m.depublish();
		
		server.join();
	}
	
	@Test
	public void testPOPNoParamNoReturn(){		
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < REPETITIONS; i++){
			object.noParamNoReturn();
		}
		
		//System.out.println("testPOPNoParamNoReturn() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testPOPNoParamSimple(){		
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < REPETITIONS; i++){
			assertEquals(100, object.noParamSimple());
		}
		
		//System.out.println("testPOPNoParamSimple() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testPOPNoParamComplex(){
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < REPETITIONS; i++){
			assertEquals(3, object.noParamComplex().length);
		}
		
		//System.out.println("testPOPNoParamComplex() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testPOPSimpleParam(){
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < REPETITIONS; i++){
			object.simpleParam(100);
		}
		
		//System.out.println("testPOPSimpleParam() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testPOPComplexParam(){
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < REPETITIONS; i++){
			object.complexParam(COMPLEX_PARAM);
		}
		
		//System.out.println("testPOPComplexParam() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testSocketNoParamNoReturn() throws UnknownHostException, IOException, InterruptedException{
		
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < REPETITIONS; i++){
			con.noParamNoReturn();
		}
		//System.out.println("testSocketNoParamNoReturn() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testSocketNoParamSimple() throws UnknownHostException, IOException, InterruptedException{
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < REPETITIONS; i++){
			assertEquals(100, con.noParamSimple());
		}
		//System.out.println("testSocketNoParamSimple() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testSocketNoParamComplex() throws UnknownHostException, IOException, InterruptedException{
		long start = System.currentTimeMillis();
		for(int i = 0; i < REPETITIONS; i++){
			assertEquals(3, con.noParamComplex().length);
		}
		//System.out.println("testSocketNoParamComplex() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testRMINoParamNoReturn() throws NotBoundException, IOException{
		long start = System.currentTimeMillis();
		for(int i = 0; i < REPETITIONS; i++){
			obj.noParamNoReturn();
		}
		//System.out.println("testRMINoParamNoReturn() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testRMINoParamSimple() throws NotBoundException, IOException{
		long start = System.currentTimeMillis();
		for(int i = 0; i < REPETITIONS; i++){
			assertEquals(100, obj.noParamSimple());
		}
		//System.out.println("testRMINoParamSimple() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testRMINoParamComplex() throws NotBoundException, IOException{
		long start = System.currentTimeMillis();
		for(int i = 0; i < REPETITIONS; i++){
			assertEquals(3, obj.noParamComplex().length);
		}
		//System.out.println("testRMINoParamComplex() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testRMISimple() throws IOException{
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < REPETITIONS; i++){
			obj.simpleParam(100);
		}
		
		//System.out.println("testRMISimple() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testRMIComplex() throws IOException{
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < REPETITIONS; i++){
			obj.complexParam(COMPLEX_PARAM);
		}
		
		//System.out.println("testRMIComplex() "+(System.currentTimeMillis() - start)+" ms");
	}
}
