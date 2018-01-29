.. |br| raw:: html

   <br />

.. _mixed:

Developing POP-Java and POP-C++ mixed applications
==================================================


POP-Java and POP-C++ interoperability
-------------------------------------

POP-Java can use POP-C++ parallel classes and POP-C++ can also use POP-Java
parallel classes. This chapter will explain everything the programmer needs to
know to develop mixed POP applications.


Restrictions
------------

As Java and C++ are different languages, there are some restrictions. In this
section, all the restrictions or programming tips will be given.


Java primitives
---------------

As Java primitives are always passed by value, the is no way to modify a Java
primitive in a POP-C++ object. In pure POP-C++ the programmer can deal with the
passing by reference but not in POP-Java.


Parameters passing
~~~~~~~~~~~~~~~~~~

Some parameters cannot be passed from a POP-Java application to a POP-C++
parallel object and vice versa. The list below explain the restrictions on
certain primitive types. The Java primitive types are taken as the basis.

* ``byte``: This type does not exist in C++ so it's not possible to pass a
  byte.
* ``long``: The Java long is coded on 8 bytes as it's coded on 4 bytes with
  C++. Some unexpected behavior can occur.
* ``char[]``: The char array cannot be used in the current version of POP-Java
  with POP-C++ parallel classes.


Dealing with array
~~~~~~~~~~~~~~~~~~

Passing arrays from POP-Java to POP-C++ is a bit tricky. As POP-Java and
POP-C++ do not behave the same with arrays, the programmer must be aware of the
way to pass the array. Here is an example of a method with an array as
parameter.

**The method declaration in POP-C++** |br|
In POP-C++, the programmer must give the array size to the compiler.

.. code-block:: java

   sync seq void changeIntArray(int n, [in, out, size=n] int *i);

**Method declaration in POP-Java** |br|
As POP-C++ will need the size of the array, POP-Java must declare this size as
well.

.. code-block:: java

   @POPSyncSeq
   public void changeIntArray(int n, int[] i){}

**Method call from POP-Java** |br|
In the POP-Java application, the programmer needs to give the array size in the
method call.

.. code-block:: java

   p.changeIntArray(iarray.length, iarray);


POP-Java application using POP-C++ parallel objects
---------------------------------------------------

This section will teach the programmer how to develop a POP-Java application
with a POP-C++ parallel class. The same example of the parallel class Integer
will be used.  For more details about the POP-C++ programming please have a
look to "Parallel Object Programming C++ - User and Installation Manual"
:cite:`popc_intro`. In the following example, the main class used is the same
as the one shown in the :ref:`previous chapter <testintegermain>`. All the sources
can be found in the directory ``example/mixed1`` of the POP-Java distribution.


Develop the POP-C++ parallel class
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

First, the programmer needs to write the parallel class in POP-C++ as it should
be for a POP-C++ application. The code snippet below shows the header file of
the parclass Integer:

.. _integer-ph:
.. code-block:: cpp
   :linenos:

   parclass Integer
   {
      classuid(1000);
   public:
      Integer();
      ~Integer();

      mutex void Add(Integer &other);
      conc int Get();
      seq async void Set(int val);

   private:
      int data;
   };

There are two rules to follow when the programmer develop
a POP-C++ parallel class for POP-Java usage.

* The parclass must declare a classuid.
* The methods must be declared in alphabetics order.

The next code snippet shows the implementation of the parallel class
``Integer``. There is no important information in this file for the POP-Java
usage.

.. code-block:: cpp
   :linenos:

   #include <stdio.h>
   #include "integer.ph"
   #include <unistd.h>

   Integer::Integer() {
      printf("Create remote object Integer on %s\n",
             (const char *)POPSystem::GetHost());
   }

   Integer::~Integer() {
      printf("Destroying Integer object...\n");
   }

   void Integer::Set(int val) {
      data=val;
   }

   int Integer::Get() {
      return data;
   }

   void Integer::Add(Integer &other) {
      data += other.Get();
   }
   @pack(Integer);


**Compilation of the parallel class** |br|
Once the parclass implementation is finished, it can be compiled with the
POP-C++ compiler. The following command will create an object executable of our
parclass Integer.

::

   popcc -object -o integer.obj integer.cc integer.ph


Create the partial POP-Java parallel class
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To be used in a POP-Java application, a POP-C++ parallel class must have its
partial implementation in POP-Java language. A partial implementation means
that all the methods must be declared but does not need to be implemented.

The next code snippet shows the partial implementation of the parallel class
``Integer``. All the methods are just declared. This partial implementation is
a translation of the POP-C++ source code to POP-Java source code.

.. code-block:: java
   :linenos:

   @POPClass
   public class Integer {
      private int value;

      public Integer() {
      }

      @POPSyncMutex
      public void add(Integer i) {
      }

      @POPSyncConc
      public int get() {
         return 0;
      }

      @POPAsyncSeq
      public void set(int val) {
      }
   }

.. note::

   In the future version of POP-C++ and POP-Java, the partial
   implementation would be generated by the compiler. For the moment, the
   programmer will need to do it by hand.


Special compilation
~~~~~~~~~~~~~~~~~~~

To compile the partial POP-Java parallel class, the compiler needs some
additional information. The POP-Java compiler has an option to generate an
additional information XML file. To generate this file use the following
command line::

   popjc -x Integer.pjava

This command will generate a file (``additional-infos.xml``) in the current
directory. This file is incomplete. The programmer will need to edit it with
the information of the POP-C++ parallel class. The following snippet shows the
file generated by the POP-Java compiler:

.. _additional-infos-xml:
.. code-block:: xml

   <popjparser-infos>
      <popc-parclass file="Integer.pjava" name="" classuid=""
                     hasDestructor="true"/>
   </popjparser-infos>

The two empty attributes ``name`` and ``classuid`` must be completed with the
value of the POP-C++ parallel class. An example of how the complete file must
look like is given below:

.. code-block:: xml

   <popjparser-infos>
      <popc-parclass file="Integer.pjava" name="Integer" classuid="1000" 
                     hasDestructor="true"/>
   </popjparser-infos>

All the information to compile the POP-Java application is now known. Here is
the command to compile it:

**Compilation as .class files**

::

   popjc -p additional-infos.xml Integer.pjava TestInteger.pjava

**Compilation as .jar file**

::

   popjc -j myjar.jar -p additional-infos.xml Integer.pjava TestInteger.pjava


Generate the object map
~~~~~~~~~~~~~~~~~~~~~~~

An object map is also needed for a POP-Java application using POP-C++ parallel
classes. The programmer can generate this object map with the POP-Java
application launcher and the option ``--listlong``. This option also accepts the
POP-C++ executable files. Here is the command used for the example
application::

   popjrun --listlong integer.obj > objmap.xml


Generated objmap.xml file (path and architecture can differ from the ones shown
here): 

.. code-block:: xml

   <CodeInfoList>
      <CodeInfo>
         <ObjectName>Integer</ObjectName>
         <CodeFile>/home/clementval/pop/popjava-1.0/example/mixed/
         integer.obj</CodeFile>
         <PlatForm>i686-pc-Linux</PlatForm>
      </CodeInfo>
   </CodeInfoList>


Running the application
~~~~~~~~~~~~~~~~~~~~~~~

To run the mixed application, the programmer needs to use the POP-Java
application launcher. As the application main class is written in POP-Java,
only this tool can run this application. Here is the command used to run the
application::

   popjrun objmap.xml TestInteger

The output of the example application should be like the following::

   i1=23
   i2=25
   i1+i2=48
   Test Integer Successful 

If any problems occurred with the compilation or the launching of the
application, please see the chapter :ref:`trouble`.


POP-C++ application using POP-Java parallel objects
---------------------------------------------------

A POP-C++ application can also use POP-Java parallel classes. The following
chapter shows how to develop, compile and run a POP-C++ using POP-Java parallel
objects.


Developing and compiling the POP-Java parallel class
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The POP-Java parallel class will be the same as the one shown in the
:ref:`previous chapter <testintegerclass>`. The compilation will be a little
bit different. As for a POP-Java application using a POP-C++ parclass, the
POP-Java will need some additional informations during the compilation process.
These additional information must be given in a XML file. The POP-Java
compiler can generate a canvas of this file with the option "-x". Here is the
command we used::

   popjc -x Integer.pjava

The generated file will be similar to the one shown in the
:ref:`Special compilation section <additional-infos-xml>`. This time the
attribute ``name`` must stay empty as we want to keep the real name of the
POP-Java parallel class. The completed file should look like in the following
snippet:

.. code-block:: xml

   <popjparser-infos>
      <popc-parclass file="Integer.pjava" name="" classuid="1000" 
                     hasDestructor="true"/>
   </popjparser-infos>

This file can be given to the compiler to compile the parallel class with the
following command::

   popjc -p additional-infos.xml Integer.pjava


The POP-C++ partial implementation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

As for the POP-Java application using POP-C++ parallel objects, the POP-C++
application will need a partial implementation of the parallel class in
POP-C++. The header file will stay the same as the one shown
:ref:`previously <integer-ph>`. The code snippet below shows
the partial implementation of the POP-C++ parallel class. Once again, the
methods are declared but not implemented.

.. code-block:: cpp
   :linenos:

   #include <stdio.h>
   #include "integer.ph"
   #include <unistd.h>

   Integer::Integer() {
      printf("Create remote object Integer on %s\n",
             (const char *)POPSystem::GetHost());
   }

   Integer::~Integer() {
   }

   void Integer::Set(int val) {
   }

   int Integer::Get() {
      return 0;
   }

   void Integer::Add(Integer &other) {
   }
   @pack(Integer);


The POP-C++ main
~~~~~~~~~~~~~~~~

To be able to run the application, a ``main`` function must be written. An
example of such a function is given below:

.. code-block:: cpp
   :linenos:

   #include "integer.ph"
   #include <iostream>
   using namespace std;
   int main(int argc, char **argv)
   {
      try{
         // Create 2 Integer objects
         Integer o1;
         Integer o2;
         o1.Set(1); o2.Set(2);
         cout << endl << "o1="<< o1.Get() << "; o2=" << o2.Get() << endl;
         cout<<"Add o2 to o1"<<endl;
         o1.Add(o2);
         cout << "o1=o1+o2; o1=" << o1.Get() << endl << endl;
      } catch (POPException *e) {
         cout << "Exception occurs in application :" << endl;
         e->Print();
         delete e;
         return -1;
      } // catch
      return 0;
   }

The main is very similar to the one used in POP-Java but this time it is
written in POP-C++.


Object map
~~~~~~~~~~

As the current version of POP-C++ is not able to generate the object map for a
POP-Java parallel class, the programmer needs to edit the object map manually.

The code below is the canvas of the line to add in a POP-C++ object map for a
POP-Java parallel class.

::

   POPCObjectName *-* /usr/bin/java -cp POPJAVA_LOCATION
   popjava.broker.Broker -codelocation=CODE_LOCATION
   -actualobject=POPJAVAObjectName

Here is the line for the example (the path will be different on your computer):

::

   Integer *-* /usr/bin/java -cp /home/clementval/popj
   popjava.broker.Broker
   -codelocation=/home/clementval/pop/popjava-1.0/example/mixed2
   -actualobject=Integer


Compile and run the POP-C++ application
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The POP-Java parallel class is compiled and the object map is complete.
The main and the partial implementation of the parallel class in POP-C++ must
be compiled. The following command will compile our application::

   popcc -o main integer.ph integer.cc main.cc
   popcc -object -o integer.obj integer.cc integer.ph main.cc

Everything is compiled and we can run the application with the "popcrun" tool::

   popcrun obj.map ./main

The output of the application should look like this::

   popcrun obj.map ./main

   o1=1; o2=2
   Add o2 to o1
   o1=o1+o2; o1=3
