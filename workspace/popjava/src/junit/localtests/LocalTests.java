package junit.localtests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.localtests.annotations.AnnotationsTest;
import junit.localtests.annotations.POPObjectDefaultMethodTest;
import junit.localtests.arrays.ArraysTest;
import junit.localtests.bigData.BigDataTests;
import junit.localtests.callback.CallBackTest;
import junit.localtests.concurrency.TestConcurrency;
import junit.localtests.creation.NestedPOPCreation;
import junit.localtests.deamontest.DeamonTest;
import junit.localtests.enums.EnumTests;
import junit.localtests.integer.IntegerTest;
import junit.localtests.jobmanager.POPJavaJobManagerConfigurationTest;
import junit.localtests.jobmanager.POPJavaJobManagerLiveConfigurationTest;
import junit.localtests.jvmObject.JVMObjectTest;
import junit.localtests.parameters.ParameterTests;
import junit.localtests.readerWriter.ReaderWriterTest;
import junit.localtests.referencePassing.ReferenceTest;
import junit.localtests.security.CreateKeyStoreTest;
import junit.localtests.security.MethodAccessTest;
import junit.localtests.subclasses.SubclassingTest;
import junit.localtests.subclasses.CallFromSubClassTest;
import junit.localtests.protocols.ProtocolsTests;
import junit.localtests.protocols.IncompatibleConnectionsTest;
import junit.localtests.protocols.WhiteBlacklistTest;
import junit.localtests.serializable.JavaSerializableTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CallBackTest.class, 
	IntegerTest.class, 
	AnnotationsTest.class, 
	POPObjectDefaultMethodTest.class,
	ReaderWriterTest.class,
	TestConcurrency.class, 
	DeamonTest.class, 
	EnumTests.class,
	ArraysTest.class, 
	ParameterTests.class,
	BigDataTests.class, 
	SubclassingTest.class,
	CallFromSubClassTest.class,
	ReferenceTest.class, 
	JVMObjectTest.class, 
	NestedPOPCreation.class,
	CreateKeyStoreTest.class, 
	MethodAccessTest.class,
	POPJavaJobManagerConfigurationTest.class, 
	POPJavaJobManagerLiveConfigurationTest.class,
	ProtocolsTests.class, 
	IncompatibleConnectionsTest.class, 
	WhiteBlacklistTest.class, 
	JavaSerializableTest.class
})
public class LocalTests {

}
