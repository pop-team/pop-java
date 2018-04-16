package ch.icosys.popjava.junit.localtests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.icosys.popjava.junit.localtests.accounting.AccountingAPITest;
import ch.icosys.popjava.junit.localtests.accounting.AccountingTest;
import ch.icosys.popjava.junit.localtests.annotations.AnnotationsTest;
import ch.icosys.popjava.junit.localtests.annotations.POPObjectDefaultMethodTest;
import ch.icosys.popjava.junit.localtests.arrays.ArraysTest;
import ch.icosys.popjava.junit.localtests.bidirectional.BiDirectionalTest;
import ch.icosys.popjava.junit.localtests.bigData.BigDataTests;
import ch.icosys.popjava.junit.localtests.callback.CallBackTest;
import ch.icosys.popjava.junit.localtests.concurrency.TestConcurrency;
import ch.icosys.popjava.junit.localtests.creation.NestedPOPCreation;
import ch.icosys.popjava.junit.localtests.deamontest.DeamonTest;
import ch.icosys.popjava.junit.localtests.enums.EnumTests;
import ch.icosys.popjava.junit.localtests.integer.IntegerTest;
import ch.icosys.popjava.junit.localtests.jobmanager.POPJavaJobManagerConfigurationTest;
import ch.icosys.popjava.junit.localtests.jobmanager.POPJavaJobManagerLiveConfigurationTest;
import ch.icosys.popjava.junit.localtests.jvmObject.JVMObjectTest;
import ch.icosys.popjava.junit.localtests.parameters.ParameterTests;
import ch.icosys.popjava.junit.localtests.priority.MethodAnnotationPriority;
import ch.icosys.popjava.junit.localtests.protocols.IncompatibleConnectionsTest;
import ch.icosys.popjava.junit.localtests.protocols.ProtocolsTests;
import ch.icosys.popjava.junit.localtests.protocols.WhiteBlacklistTest;
import ch.icosys.popjava.junit.localtests.readerWriter.ReaderWriterTest;
import ch.icosys.popjava.junit.localtests.referencePassing.ReferenceTest;
import ch.icosys.popjava.junit.localtests.security.CreateKeyStoreTest;
import ch.icosys.popjava.junit.localtests.security.MethodAccessTest;
import ch.icosys.popjava.junit.localtests.serializable.JavaSerializableTest;
import ch.icosys.popjava.junit.localtests.subclasses.CallFromSubClassTest;
import ch.icosys.popjava.junit.localtests.subclasses.SubclassingTest;

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
	JavaSerializableTest.class,
	AccountingTest.class,
	AccountingAPITest.class,
	MethodAnnotationPriority.class,
	BiDirectionalTest.class
})
public class LocalTests {

}
