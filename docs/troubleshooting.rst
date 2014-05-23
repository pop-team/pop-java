.. _trouble:

Troubleshooting
===============

POP-Java exception
------------------

This section lists some of the main POP-Java exception that can occurred during
the application execution and gives the cause of the problem.


Cannot bind to access point: socket://your-computer-name:2711
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This exception occurs when the application cannot contact the POP-C++ runtime
system. To fix this problem, we need to start the POP-C++ runtime system with
the following command::

   POPC_LOCATION/sbin/SXXpopc start


Error message: ``OBJECT_EXECUTABLE_NOTFOUND``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This exception occurs when the executable file is not found. This might be
due to a bad object map or the deletion of the object executable file. To fix
this problem we should generate a new object map with the object executable.


Error message: ``NO_RESOURCE_MATCH``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This exception occurs when no resource match the requirements of a specific
object. To fix this problem we should check the object descriptions in the
parallel objects. We might have put a too high requirement for a parallel
object creation.


Error message: ``Cannot run program "/usr/local/popc/services/appservice"``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If we get an error with "cannot run program" and the path contains the
appservice of POP-C++, you have certainly reinstalled POP-C++ and the
configuration file of POP-Java is now wrong. The easiest way to fix this
problem is to reinstall also POP-Java. We can also edit the configuration file
under ``POPJAVA_LOCATION/etc/popj_config.xml``. The item 
``popc_appcoreservice_location`` must be modified with the good path.


Test suite frozen
~~~~~~~~~~~~~~~~~

If the test suite seems to be frozen, we should abort the test suite and
restart the POP-C++ global service with the following command::

   POPC_LOCATION/sbin/SXXpopc restart
