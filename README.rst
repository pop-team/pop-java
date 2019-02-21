POP-Java 2.0
============

Version 2.0 is under development, use it at your own risk.

Quickstart
----------------------------
POP-Java can be used in a Java application in 2 ways.
Either way, when running your application you need to add the pop-java.jar file as the jvm javaagent when launching:

.. code::
  java -javaagent:popjava.jar ....

Custom compiled Jar
~~~~~~~~

Compile the pop-java jar file

.. code::

  $ ./gradlew fatJar

Add the created jar to the classpath of your project.

Maven
~~~~~~~~

Add the following to your pom.xml:

.. code::

	<dependency>
		<groupId>ch.icosys</groupId>
		<artifactId>popjava</artifactId>
		<version>2.0.0</version>
	</dependency>

COMPILE POP-Java SOURCE CODE
----------------------------

By using the command below you will build and test the latest version of POP-Java.

.. code::

  $ ./gradlew build
  
In case you don't want to wait you can skip the JUnit test by running the following command.

.. code::

  $ ./gradlew build -x test


INSTALLING POP-Java
-------------------

.. code::

  $ ./gradlew prepareRelease
  $ ./install

You might use sudo or to be root tu execute "make install" if you do not have the right to write in the destination folder.

The installation script will ask you several questions. Here they are:

1. Please enter the location of your desired POP-Java installation (default: /usr/local/popj ) :

If unsure leave the answer blank to keep the default setting.

STARTING POP-Java
-----------------

**Do not forget to add these lines to your .bashrc file or equivalent :**
If you have kept the default options this should give you something like:

.. code::
  
  POPJAVA_LOCATION=/usr/local/popj
  export POPJAVA_LOCATION
  POPJAVA_JAVA=/usr/bin/java
  export POPJAVA_JAVA
  PATH=$PATH:$POPJAVA_LOCATION/bin
  
DEVELOPMENT
-----------

Depending on the environment you use you may want to use different IDE, or none at all.
Gradle can setup the environment for different platforms.

Eclipse
~~~~~~~

Go in the project root and generate the Eclipse project files.

.. code::

  $ ./gradlew eclipse
  
You now should be able to open POP-Java as a Java project.

IntelliJ IDEA
~~~~~~~~~~~~~

Go in the project root and generate the IntelliJ project files.

.. code::

  $ ./gradlew idea
  
You now should be able to open POP-Java as a Java project.

Netbeans
~~~~~~~~

Open the project as a Gradle project. Nothing more should be required.


LICENCE
-------
POP-Java is licenced under LGPL v3

RESOURCES
---------
* The user manual `<http://pop-java.readthedocs.io/en/latest/>`_
