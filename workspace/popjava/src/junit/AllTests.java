package junit;

import junit.annotations.AnnotationTests;
import junit.annotations.od.TestOD;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { AnnotationTests.class})
public class AllTests {

}
