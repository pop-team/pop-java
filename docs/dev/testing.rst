
Testing
=======

There are two types of tests, JUnit and environment dependant tests.

JUnit
-----

This kind of tests are used to see if many expected behaviors don't changes over time.

This kind of tests can tricky because contrarily to Test Suite tests, all of the JUnit tests a

Create a new test
~~~~~~~~~~~~~~~~~

In the ``junit`` package in the POP-Java workspace look for an appropriate package or create a new one to host a new test.

Use the following template to start creating a test class. It's important that each unit test initialize and end the POP Environment, the methods marked with ``@Before`` and ``@After`` do exactly this. For further information in regards how JUnit works visit `JUnit's documentation <http://junit.org/junit4/>`_. ::

    public class SomeTests {

        @Before
        public void initPOP() {
            POPSystem.initialize();
        }

        @After
        public void endPOP() {
            POPSystem.end();
        }

        @Test
        public void myTest() {
            ...
            assertTrue(...)
        }
    }

.. note:: As of now we are using JUnit4, when POP-Java will use Java 8 as a minimum platform we will probably upgrade.


After the test is written don't forget to add it to the Test Suite. For example ::

    @Suite.SuiteClasses( { ..., SomeTests.class})
    public class LocalTests

Peculiarities
~~~~~~~~~~~~~

The is one extra details we have to be on alert with writing JUnit tests, all POP Object apart from having the ``@POPClass`` annotation should also extends POPObject directly.
Furthermore, all new POP Object create must use the ``PopJava.newInstance`` method since there is no Java Agent running in the JUnit tests. ::

    @POPClass
    class MyPOP extends POPObject {
        void MyPOP() { }
    }

    class MyTest {
        ... // before & after
        @Test
        public void test() {
            MyPOP my = PopJava.newInstance(MyPOP.class);
            ...
        }
    }

Test Suite
----------

The POP-Java Test Suite is a Shell Script with the objective of executing some small POP-Java program in a configured POP Environment.

.. todo:: continue