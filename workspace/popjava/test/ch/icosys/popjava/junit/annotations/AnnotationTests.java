package junit.annotations;

import junit.annotations.od.TestOD;
import junit.annotations.semantics.TestSemantics;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { TestOD.class, TestSemantics.class})
public class AnnotationTests {

}
