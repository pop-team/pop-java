.. _compileandrun:

Compile and run a POP-Java application
======================================

This chapter explains the POP-Java compilation process, the POP-Java
application launching process and the tools related to those processes. The
structure of this chapter is as follows: The first section explains the
compilation process. The second describes the application launching tools.
The third one aims to help the programmer to
understand the importance of the object map and the object map generator in the
POP-Java application launching process. Finally, a full example is explained to
pass through the whole process.


POP-Java compilation
--------------------

POP-Java uses the standard Java compiler and can easily be integrated into an existing compilation process.
As POP-Java java files use features from the POP-Java library, the POP-Java jar file needs to be included in the classpath during the compilation process.
You find examples of how to do this below:

Maven
~~~~~

.. code::

	<dependency>
		<groupId>ch.icosys</groupId>
		<artifactId>popjava</artifactId>
		<version>2.0.0</version>
	</dependency>

Ant
~~~

.. code-block:: xml

	<property environment="env"/>

	<target name="build" description="compile the source " >
		<javac srcdir="${source.folder}"
			destdir="${class.folder}"
			classpath="${env.POPJAVA_LOCATION}/JarFile/popjava.jar"
		/>
	</target>

Full example
------------

This section shows how to write, compile and launch a POP-Java application by
using a simple example. The POP-Java application used in this example includes
only one parallel class. All sources of this example can be found in the
directory ``examples/integer`` from the POP-Java distribution.

Programming
~~~~~~~~~~~

When we start to develop a POP-Java application the main part is the parallel
classes. The following code snippet shows the parallel class implementation:

.. _testintegerclass:
.. code-block:: java
   :linenos:

   import ch.icosys.popjava.core.annotation.*;
   
   @POPClass
   public class Integer {
       private int value;

       @POPObjectDescription(url="localhost")
       public Integer() {
           value = 0;
       }

       @POPSyncConc
       public int get() {
           return value;
       }

       @POPAsyncSeq
       public void set(int val) {
           value = val;
       }

       @POPAsyncMutex
       public void add(Integer i) {
           value += i.get();
       }
   }

As we can see this class uses special POP-Java keywords. In the line 1, the
parclass keyword specifies that this class is a parallel class. The constructor
declaration includes an object description (line 4). The method declarations
includes the invocation semantics (line 8, 12 and 16). The method ``add``
(line 16) receive another parallel object as a parameter and it's transparent
for the programmer.

Once the parallel class is implemented, we can write a main class that use this
parallel class. The following code snippet shows the code of the main class:

.. _testintegermain:
.. code-block:: java
   :linenos:

   import ch.icosys.popjava.core.annotation.*;

   @POPClass(isDistributable = false)
   public class TestInteger {
       public static void main(String[] args){
           Integer i1 = new Integer();
           Integer i2 = new Integer();
           i1.set(23);
           i2.set(25);
           System.out.println("i1=" + i1.get());
           System.out.println("i2=" + i2.get());
           i1.add(i2);
           int sum = i1.get();
           System.out.println("i1+i2 = "+sum);
           if(sum==48)
               System.out.println("Test Integer Successful");
           else
               System.out.println("Test Integer failed");
       }
   }


The code of the main class is pure Java code. 
The instantiation (lines 3-4) and the method calls (lines 5-9) are
transparent for the programmer.


Compiling
~~~~~~~~~

To manually compile the source files, use the following command:

**Compiling as .class files**

::
   javac -cp $POPJAVA_LOCATION/JarFile/popjava.jar Integer.java TestInteger.java

Running
~~~~~~~

Run the application normally, just by adding 

::

  java -javaagent:$POPJAVA_LOCATION/JarFile/popjava.jar -cp myjar.jar TestInteger

**Application output**

Here is what we should have as the application output::

  i1=23
  i2=25
  i1+i2=48
  Test Integer Successful

If the are any problems with the compilation or the launching of the
application, please refer to the chapter :ref:`trouble`.

Misc
~~~~

If you are running a POP-Java application on a computer with multiple network interfaces, make sure you specify the network interface to use.
To specify the name of the network interface, set the ``POPJ_IFACE`` environment variable.
If the specified name is not found, POP-Java will fall back to the same behaviour as if no network interface was specified as default.
