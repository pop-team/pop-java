package ch.icosys.popjava.junit.benchmarks;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.icosys.popjava.junit.benchmarks.methods.TestMethods;
import ch.icosys.popjava.junit.benchmarks.readerWriter.Benchmark;

@RunWith(Suite.class)
@Suite.SuiteClasses( { Benchmark.class, TestMethods.class })
public class BenchmarkTests {

}
