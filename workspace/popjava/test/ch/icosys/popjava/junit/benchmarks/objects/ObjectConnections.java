package ch.icosys.popjava.junit.benchmarks.objects;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;
import ch.icosys.popjava.core.util.ssl.KeyPairDetails;
import ch.icosys.popjava.core.util.ssl.KeyStoreDetails;
import ch.icosys.popjava.core.util.ssl.SSLUtils;

@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "benchmark-methods")
@BenchmarkHistoryChart(filePrefix = "benchmark-methods-history", maxRuns = 10)
public class ObjectConnections extends AbstractBenchmark{
	
	private static MyObject obj;

	@BeforeClass
	public static void setup() throws IOException {;
	
		final File config = new File("tfc_config-test");
	
		if(!config.exists()){
	        config.createNewFile();
	    }
	
	    Configuration.getInstance().load(config);
	
	    KeyStoreDetails ksOptions = new KeyStoreDetails("asdf12341", "12331423423", new File("tfc-test").getAbsoluteFile());
	    KeyPairDetails keyOptions = new KeyPairDetails("alias");
	    
	    SSLUtils.generateKeyStore(ksOptions, keyOptions);
	    Configuration.getInstance().setSSLKeyStoreOptions(ksOptions);
	    Configuration.getInstance().setDefaultNetwork(keyOptions.getAlias());
	
	    Configuration.getInstance().store();
    
		POPSystem.initialize();
		obj = PopJava.newActive(null, MyObject.class);
		obj.test();
	}
	
	@Test
	@BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
	public void testConnections() {
		MyObject temp = PopJava.newActive(null, MyObject.class, obj.getAccessPoint());
	}
	
	@Test
	@BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 1)
	public void testConnectionsMethodCall() {
		obj.test();
	}
	
	@AfterClass
	public static void shutdown() {
		POPSystem.end();
	}
}
