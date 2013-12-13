package junit;

import junit.annotations.AnnotationTests;
import junit.localtests.LocalTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { AnnotationTests.class, LocalTests.class})
public class AllTests {

}
