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
---------------------

POP-Java uses the standard Java compiler and can easily be integrated into an existing compilation process.
As POP-Java java files use features from the POP-Java library, the POP-Java jar file needs to be included in the classpath during the compilation process.
An example of how to compile POP-Java source-code using ant is shown below:

.. code-block:: ant

	<property environment="env"/>

	<target name="build" description="compile the source " >
		<javac srcdir="${source.folder}"
			destdir="${class.folder}"
			classpath="${env.POPJAVA_LOCATION}/JarFile/popjava.jar"
		/>
	</target>


The POP-Java application launcher
---------------------------------

To help POP-Java programmer, POP-Java provides an application launcher that
simplifies the launch of a POP-Java application. This application launcher is
named ``popjrun`` and is used with the following syntax::

  popjrun <options> [<objectmap>] <MainClass> <arguments>

Here is an explanation of the arguments to provide to the POP-Java application
launcher:

* ``options``: in the current version there is only one option ``-c`` or
  ``--classpath`` that allow the programmer to add some class path for the
  execution of the POP-Java application. The different class paths must be
  separated with a semicolon.
* ``objectmap``: this informations is not mandatory. If it's provided, the
  object map informs the runtime system about the location of the different
  compiled parallel classes of the application. If it's not provided, the
  default object map (located at:
  ``{POPJAVA_LOCATION}/etc/defaultobjectmap.xml``) will be used. More
  information give in :ref:`objectmap`.
* ``MainClass``: this is a main class of the POP-Java application.
* ``arguments``: these are the arguments of the program.


.. _objectmap:

The POP-Java object map and object map generator
------------------------------------------------

The object map is an XML file that informs the POP-C++ runtime about the
location of the different compiled parallel classes of the application. This
file can be given to the "popjrun" tool. If the programmer does not specify this
file, the default object map located at ``{POPJAVA_LOCATION}/etc/`` will be
used.

The object map can be generated with the POP-Java application launcher. By
using the option ``-l`` or ``--listlong`` and giving the class files or the JAR
file, the object map will be printed to the standard output. The easiest way to
save this file is to redirect the output into the desired file.

Here are the commands used for our example:

**Compiled classes**

::

  popjrun --listlong Parclass1.class:Parclass2.class > objectmap.xml

**JAR file**

::

  popjrun --listlong parclasses.jar > objectmap.xml
  
An example of a generated objectmap can be found here: :ref:`objectmap`.
The objectmap contains the path to the compiled classfile for every POP-Java class passed to the popjrun command.
The path can either be a path to a folder containing the class file, or a jar file containing the class file.
The path can either be a local path, or a url accessible by http. Keep in mind that all paths indicated
need to be accessible by every machine that will create a POP-Java object.

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
   import popjava.annotation.*;
   
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
   import popjava.annotation.*;

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


Create the object map
~~~~~~~~~~~~~~~~~~~~~

Before running the example application, the programmer needs to generate the
object map. The object map will be given to the POP-Java launcher which
will inform the POP-C++ runtime system where to find the compiled files.
The specified path needs to be accessible on every machine where an object of that type
is initialized. The POP-Java launcher has a specific option to generate this file from the compiled
files (``.class``) or the JAR file (``.jar``). Here is the command used for our
example::

   popjrun --listlong Integer.class > objmap.xml

The command will generate the XML file and print it on the standard output. To
save this file, we redirect the output in a file named objmap.xml. This file
contains the following XML code (the path specified in the element CodeFile
will be different on your computer):

.. example-objectmap:
.. code-block:: xml

   <CodeInfoList>
     <CodeInfo>
       <ObjectName>Integer</ObjectName>
       <CodeFile Type="popjava">
         /home/clementval/pop/popjava-1.0/example/integer/</CodeFile>
       <PlatForm>*-*</PlatForm>
     </CodeInfo>
   </CodeInfoList>


Running
~~~~~~~

Once the POP-Java application is compiled and the object map is generated, the
application can be run. A POP-Java application is a pure Java application at
the end and could be run with the standard java program. In order to make this
running easier for the programmer, POP-Java includes an application launcher.
Here are the commands to use to run the POP-Java application example.
At the end an example is given on how run the POP-Java application directly through Java.

**POP-Java application compiled as .class files**

::

  popjrun objectmap.xml TestInteger


**POP-Java application compiled as .jar file**

::

  popjrun -c myjar.jar objectmap.xml TestInteger
  
**POP-Java application run directly through java**

::
	java -agent:$POPJAVA_LOCATION/JarFile/popjava.jar -cp $POPJAVA_LOCATION/JarFile/popjava.jar:. TestInteger

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
