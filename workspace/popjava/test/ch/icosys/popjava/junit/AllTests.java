package junit;

import junit.annotations.AnnotationTests;
import junit.localtests.LocalTests;
import junit.system.SystemTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
	AnnotationTests.class, 
	LocalTests.class, 
	SystemTests.class
})
public class AllTests {

}
