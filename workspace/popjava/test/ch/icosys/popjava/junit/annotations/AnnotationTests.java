package ch.icosys.popjava.junit.annotations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.icosys.popjava.junit.annotations.od.TestOD;
import ch.icosys.popjava.junit.annotations.semantics.TestSemantics;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
	TestOD.class, 
	TestSemantics.class })
public class AnnotationTests {
}
