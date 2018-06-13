package ch.icosys.popjava.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.icosys.popjava.junit.annotations.AnnotationTests;
import ch.icosys.popjava.junit.localtests.LocalTests;
import ch.icosys.popjava.junit.system.SystemTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
	//AnnotationTests.class, 
	LocalTests.class/*, 
	//SystemTests.class*/ })
public class AllTests {
}
