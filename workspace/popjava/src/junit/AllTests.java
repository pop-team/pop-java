package junit;

import junit.annotations.TestOD;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { TestOD.class})
public class AllTests {

}
