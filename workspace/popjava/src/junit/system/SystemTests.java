package junit.system;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { POPSystemTest.class, RawBufferTest.class, ConstructorTests.class, POPJavaDeamonTest.class})
public class SystemTests {
    
}
