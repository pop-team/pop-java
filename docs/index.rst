Welcome to POP-Java's documentation!
====================================

Welcome to POP-Java's documentation. POP-Java is an implementation of the POP
(**P**\ arallel **O**\ bject **P**\ rograming) model for the Java programming
language. The POP model is based on the very simple idea that objects are
suitable structures to distribute data and executable code over heterogeneous
distributed hardware and to make them interact between each other.

POP-Java is a comprehensive object-oriented system for developing HPC
applications in large distributed computing infrastructures such as Grid or
P2P. It consists of a programming suite (language, compiler) and a run-time
system for running POP-Java applications.

POP-Java language is minimal an extension of Java that implements the parallel
object model with the integration of resource requirements into distributed
objects. We try to keep this extension as close as possible to Java so that
programmers can easily learn POP-Java and that existing Java applications can
be parallelized using POP-Java without too much effort.

This documentation is divided in three parts:

* The :ref:`user-manual` targets the users of the POP-Java framework and
  describes how to develop and run POP-enabled Java applications.
* The :ref:`tfc` manual regarding what is TFC and how to use it.
* The :ref:`developer-manual` targets the developers of the POP-Java framework
  and contains guidelines and resources for the development process.
* The :ref:`references` collect various reference documents, useful for both
  users and developers alike.

.. ............................................................................

.. _user-manual:

.. toctree::
   :maxdepth: 1
   :caption: User manual

   user/introduction
   user/model
   user/applications-development
   user/running
   user/mixed-applications
   user/plugin
   user/installation
   user/configuration
   user/troubleshooting
   user/bibliography

.. _tfc:

.. toctree::
   :maxdepth: 1
   :caption: TFC

   tfc/what.rst
   tfc/usage.rst
   tfc/advanced.rst

.. ............................................................................

.. _developer-manual:

.. toctree::
   :maxdepth: 1
   :caption: Developer manual

   dev/contributing
   dev/release
   dev/documentation
   dev/architecture
   dev/brokerinterface
   dev/packages
   dev/communication
   dev/javaagent
   dev/configuration
   dev/services
   dev/annotations
   dev/testing

.. ............................................................................

.. _references:

.. toctree::
   :maxdepth: 1
   :caption: References

   POP-Java homepage <http://gridgroup.hefr.ch/popj/>
   API Reference <http://gridgroup.github.io/pop-java/api/>
   POP-Java source code <https://github.com/pop-team/pop-java>
   refs/examples
   refs/commands
   refs/releases
   POP-C++ homepage <http://gridgroup.hefr.ch/popc/>
   refs/todos
