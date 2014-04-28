package junit.benchmarks;

import junit.benchmarks.readerWriter.Benchmark;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { Benchmark.class })
public class BenchmarkTests {

}
