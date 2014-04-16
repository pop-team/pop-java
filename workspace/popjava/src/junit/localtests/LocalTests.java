package junit.localtests;

import junit.localtests.annotations.AnnotationsTest;
import junit.localtests.benchmark.Benchmark;
import junit.localtests.callback.CallBackTest;
import junit.localtests.concurrency.TestConcurrency;
import junit.localtests.deamontest.DeamonTest;
import junit.localtests.enums.EnumTests;
import junit.localtests.integer.IntegerTest;
import junit.localtests.readerWriter.ReaderWriterTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { CallBackTest.class, IntegerTest.class, AnnotationsTest.class, ReaderWriterTest.class,
	TestConcurrency.class, Benchmark.class, DeamonTest.class, EnumTests.class})
public class LocalTests {

}
