package junit.benchmarks.methods;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class TestMethods {

	private static final int REPETITIONS = 10000;
	private static POPMethods object;
	@BeforeClass
	public static void startPOPJava(){
		POPSystem.initialize();
		object = PopJava.newActive(POPMethods.class);
	}
	
	@AfterClass
	public static void endPOPJava(){
		POPSystem.end();
	}
	
	@Test
	public void testPOPNoParamNoReturn(){		
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < REPETITIONS; i++){
			object.noParamNoReturn();
		}
		
		System.out.println("testPOPNoParamNoReturn() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testPOPNoParamSimple(){		
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < REPETITIONS; i++){
			assertEquals(100, object.noParamSimple());
		}
		
		System.out.println("testPOPNoParamSimple() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testPOPNoParamComplex(){
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < REPETITIONS; i++){
			assertEquals(3, object.noParamComplex().length);
		}
		
		System.out.println("testPOPNoParamComplex() "+(System.currentTimeMillis() - start)+" ms");
	}
	
	@Test
	public void testSocketNoParamNoReturn() throws UnknownHostException, IOException, InterruptedException{
		Thread server = new Thread(new SocketServer());
		server.start();
		
		SocketConnector con = new SocketConnector();
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < REPETITIONS; i++){
			con.noParamNoReturn();
		}
		System.out.println("testSocketNoParamNoReturn() "+(System.currentTimeMillis() - start)+" ms");
		
		con.close();
		server.join();
	}
	
	@Test
	public void testSocketNoParamSimple() throws UnknownHostException, IOException, InterruptedException{
		Thread server = new Thread(new SocketServer());
		server.start();
		
		SocketConnector con = new SocketConnector();
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < REPETITIONS; i++){
			assertEquals(100, con.noParamSimple());
		}
		System.out.println("testSocketNoParamSimple() "+(System.currentTimeMillis() - start)+" ms");
		
		con.close();
		server.join();
	}
	
	@Test
	public void testSocketNoParamComplex() throws UnknownHostException, IOException, InterruptedException{
		Thread server = new Thread(new SocketServer());
		server.start();
		
		SocketConnector con = new SocketConnector();
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < REPETITIONS; i++){
			assertEquals(3, con.noParamComplex().length);
		}
		System.out.println("testSocketNoParamComplex() "+(System.currentTimeMillis() - start)+" ms");
		
		con.close();
		server.join();
	}
}
