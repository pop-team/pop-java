.. _install:

Installation
============

To use POP-Java and POP-C++ on a computer we need to install them. This chapter
helps the programmer to perform the correct installation of the POP system on a
computer.


POP-C++ installation
--------------------

In order to use POP-Java we need to install the latest version of POP-C++. This
section will help us to get trough th installation process and make sure the
installation is fine for a POP-Java usage.


Requirements
~~~~~~~~~~~~

In order to install POP-C++ we need few additional software. The following
packages are required before compiling:

* a C++ compiler (g++ or equivalent)
* zlib-devel (package name depends on distribution)
* GNU Bison (optional)
* Globus Toolkit (optional)


Before installing
~~~~~~~~~~~~~~~~~

Before installation we should make the following configuration choices. In case
of doubt the default values can be used.

* The compilation directory that should hold roughly 50MB. This directory will
  contain the distribution tree and the source files of POP-C++. It may be
  erased after installation.
* The installation directory that will hold less than 40M. It will contain the
  compiled files for POP-C++, include and configuration files. This directory
  is necessary in every computer executing POP-C++ program (default location
  ``/usr/local/popc``).
* A temporary directory will be asked in the installation process. This
  directory will be used by POP-C++ to hold file during the application
  execution (default ``/tmp``).
* Resource topology. The administrator must choose what computer form the grid.

For more informations about the POP-C++ installation and configuration process,
please see "Parallel Object Programming C++ - User and Installation Manual"
:cite:`popc_intro`.


Installation process
~~~~~~~~~~~~~~~~~~~~

This section will guide us trough the POP-C++ installation process. In the POP
distribution we find a directory including POP-C++. First, we need to configure
the installation. If we use the configure script without any option, POP-C++
will be installed in the default directory (``/usr/local/popc``). We can also
specify the directory by using the option --prefix.

**Default directory**

::

   ./configure

**Specific directory**

::

   ./configure --prefix=/home/user/popc

Once the configuration script is done, we will need to compile the source of
POP-C++ for our architecture. For this, we just need to run the make command in
the root directory of POP-C++.

::

   make

Finally, to install POP-C++, we need to run the install target of the make
file. This script will guide us trough the installation. To be sure that our
installation will fit the requirements of POP-Java, please follow the
instructions below.

Answer "y" to the first question. We need to configure POP-C++ services.

::

   make install
   ...
   DO YOU WANT TO CONFIGURE POP-C++ SERVICES? (y/n)
   y

We need to make a special installation so answer "n" to the second question::

   ...
   DO YOU WANT TO MAKE A SIMPLE INSTALLATION ? (y/n):
   n

The answers to the questions below are up to our configuration but if we don't
know our configuration just pass every question.

::

   =====================================================
   GENERATING SERVICE MAPS...
   CONFIGURING POP-C++ SERVICES ON YOUR LOCAL MACHINE...
   Enter the full qualified master host name (POPC gateway):

   Enter the child node:

   Enter number of processors available (default:1):

   Enter the maximum number of POP-C++ jobs that can run concurrently 
   (default: 100):

   Enter the available RAM for job execution in MB (default 1024) :

   Which local user you want to use for running POP-C++ jobs?

   CONFIGURING THE RUNTIME ENVIRONMENT
   Enter the script to submit jobs to the local system:

   Communication pattern:

   SETTING UP RUNTIME ENVIRONMENT VARIABLES
   Enter variable name:

We need the startup script to use the global runtime service with POP-Java so
answer "y" to the question "Do you want to generate the POP-C++ startup
scripts?".

::

   =====================================================
   CONFIGURATION POP-C++ SERVICES COMPLETED!
   =====================================================
   Do you want to generate the POP-C++ startup scripts? (y/n)
   y

Depends on our configuration, we can modify the default values of the startup
script or just keep them. One important thing is to copy the environment
variables on the .bashrc or equivalent file.

::

   =====================================================
   CONFIGURING STARTUP SCRIPT FOR YOUR LOCAL MACHINE...
   Enter the service port[2711]:

   Enter the domain name:

   Enter the temporary directory for intermediate results:

   =====================================================
   CONFIGURATION DONE!
   =====================================================

   IMPORTANT : Do not forget to add these lines to your .bashrc 
   file or equivalent :
   ---------
       POPC_LOCATION=/home/clementval/popc
       PATH=$PATH:$POPC_LOCATION/bin:$POPC_LOCATION/sbin

   Press <Return> to continue

The POP-C++ installation is done. We can now use POP-C++ and also install
POP-Java.


System startup
~~~~~~~~~~~~~~

Before executing any POP-C++ application, the runtime system (Job manager and
resource discovery) must be started. There is a script provided for that
purpose, so every node must run the following command::

   POPC_LOCATION/sbin/SXXpopc start

SXXpopc is a standard Unix deamon control script, with the traditional start,
stop and restart options.


POP-Java installation
---------------------

This section will guide us trough the POP-Java installation process.

Requirements
~~~~~~~~~~~~

In order to install POP-Java, some packages are required. Here is the list of
required packages:

* JDK 7 or higher
* POP-C++ 2.5 or higher
* JavaCC (optional)
* Apache ANT (optional)


Installation process
~~~~~~~~~~~~~~~~~~~~

To install POP-Java we need to launch the command ``ant`` int the POP-Java
directory. Once the source code is compiled, launch the installation with the
install script: ``sudo ./install``.
This script will guide us trough the installation by asking us some questions.
Be aware that if we install POP-Java in the default location we need the
administrator rights. Please use the option ``-E`` with the sudo command to
keep the environment variables.

Here is the output we should have on our shell::

   [POP-Java installation]: Detecting java executable ...
   [POP-Java installation]: Java executable detected under 
     /usr/bin/java
   [POP-Java installation]: Please enter the location of your desired 
     POP-Java installation (default: /usr/local/popj ) : 
   /home/clementval/popj
   [POP-Java installation]: Installing POP-Java under 
     /home/clementval/popj ? (y/n)
   y
   [POP-Java installation]: Copying files ...
   [POP-Java installation]: Generating configuration files ...
   [POP-Java installation]: Generating object map file for the test suite
   [POP-Java installation]: POP-Java has been installed under 
     /home/clementval/popj. Please copy the following lines into your 
     .bashrc files or equivalent

   POPJAVA_LOCATION=/home/clementval/popj
   export POPJAVA_LOCATION
   POPJAVA_JAVA=/usr/bin/java
   export POPJAVA_JAVA
   PATH=$PATH:$POPJAVA_LOCATION/bin

   [POP-Java installation]: Installation done.

At the end of the installation, the script ask us to copy some environment
variable declarations in the .bashrc or equivalent file. This step is mandatory
to make POP-Java work correctly.


Test the installation
---------------------

POP-Java includes a test suite. We can run this test suite to check if our POP
system is correctly installed. To run this test suite, we need to launch the
``launch_testsuite`` script located in the POP-Java installation location.

Here is the output we should get after the completion of the test suite::

   ./launch_testsuite 
   ########################################
   #   POP-Java 1.0 Test Suite started    #
   ########################################
   POP-C++ detected under /home/clementval/popc
   POP-C++ was not running. Starting POP-C++ runtime global services ...
   Starting POPC Job manager service: 
   POPCSearchNode access point: socket://172.28.10.67:38331
   Starting Parallel Object JobMgr service 
   socket://172.28.10.67:2711POP-C++ started
   ##############################
   #   POP-Java standard test   #
   ##############################
   Starting POP-Java test suite
   Launching passing arguments test (test 1/6)... 
   Arguments test successful
   Passing arguments test is finished ... 
   Launching multi parallel object test (test 2/6)... 
   Multiobjet test started ...
   Result is : 1234
   Multiobjet test finished ...
   Multi parallel object test is finished... 
   Launching callback test (test 3/6)... 
   Callback test started ...
   Identity callback is -1
   Callback test successful
   Callback test is finished... 
   Launching barrier test (test 4/6)... 
   Barrier: Starting test...
   Barrier test successful
   Barrier test is finished... 
   Launching integer test (test 5/6)... 
   i1 = 23
   i2 = 25
   i1+i2 = 48
   Test Integer Successful
   Integer test is finished... 
   Launching Demo POP-Java test (test 6/6)... 
   START of DemoMain program with 4 objects
   Demopop with ID=1 created with access point : socket://127.0.1.1:39556
   Demopop with ID=2 created with access point : socket://127.0.1.1:60575
   Demopop with ID=3 created with access point : socket://127.0.1.1:50088
   Demopop with ID=4 created with access point : socket://127.0.1.1:39475
   Demopop:1 with access point socket://127.0.1.1:39556 is sending his ID to object:2
   Demopop:2 receiving id=1
   Demopop:2 with access point socket://127.0.1.1:60575 is sending his ID to object:3
   Demopop:3 receiving id=2
   Demopop:3 with access point socket://127.0.1.1:50088 is sending his ID to object:4
   Demopop:4 receiving id=3
   Demopop:4 with access point socket://127.0.1.1:39475 is sending his ID to object:1
   Demopop:1 receiving id=4
   END of DemoMain program
   Demo POP-Java test is finished...

   ####################################
   #   POP-C++ interoperability test  #
   ####################################
   popcc -o main integer.ph integer.cc main.cc
   popcc -object -o integer.obj integer.cc integer.ph main.cc
   ./integer.obj -listlong > obj.map
   Launching POP-C++ integer with POP-Java application test (test 1/2)
   POPC Integer test started ...
   o1 = 10
   o2 = 20
   10 + 20 = 30
   POPC Integer test successful
   POP-C++ integer with POP-Java application test is finishied ...
   popcc -parclass-nobroker -c integer2.ph
   popcc -o main integer2.stub.o integer.ph integer.cc main.cc
   popcc -parclass-nobroker -c integer2.ph
   popcc -object -o integer.obj integer2.stub.o integer.cc integer.ph
   popcc -object -o integer2.obj integer2.cc integer2.ph
   ./integer.obj -listlong > obj.map
   ./integer2.obj -listlong >> obj.map
   Launching Integer mix (POP-C++ and POP-Java) with POP-Java application test(test 2/2)
   i=20
   j=12
   i+j=32
   Integer mix (POP-C++ and POP-Java) with POP-Java application test is finishied ...
   ########################################
   #   POP-Java 1.0 Test Suite finished   #
   ########################################
   Stopping POPC Job manager service...
   Connecting to 172.28.10.67:2711....
   POPCSearchNode stopped
   JobMgr stopped
