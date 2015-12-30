package junit.localtests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.localtests.annotations.AnnotationsTest;
import junit.localtests.arrays.ArraysTest;
import junit.localtests.callback.CallBackTest;
import junit.localtests.concurrency.TestConcurrency;
import junit.localtests.deamontest.DeamonTest;
import junit.localtests.enums.EnumTests;
import junit.localtests.integer.IntegerTest;
import junit.localtests.parameters.ParameterTests;
import junit.localtests.readerWriter.ReaderWriterTest;

@RunWith(Suite.class)
@Suite.SuiteClasses( { CallBackTest.class, IntegerTest.class, AnnotationsTest.class, ReaderWriterTest.class,
	TestConcurrency.class, DeamonTest.class, EnumTests.class,
	ArraysTest.class, ParameterTests.class})
public class LocalTests {

}
