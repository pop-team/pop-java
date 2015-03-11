package junit.system;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import popjava.interfacebase.Interface;
import popjava.system.POPSystem;

@RunWith(Suite.class)
@Suite.SuiteClasses( { POPSystemTest.class, RawBufferTest.class, ConstructorTests.class})
public class SystemTests {
    
}
