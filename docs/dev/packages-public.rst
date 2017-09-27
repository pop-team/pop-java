
Public packages and classes
===========================

This section explain the public available classes a user can use to make his POP application.

In POP the User never has to interact with the framework directly apart in very specific occasions. Which means
that the public POP API are a simple set of annotations and some helper classes, as we can see below.

If we want to add new functionality visible to the User adding parameter in one annotation or adding a method
``popjava.PopJava`` should be the preferred way to go.

In case of very specific API, like ``popjava.JobManagerConfig`` used for configuring ``POPJavaJobManager``,
it is acceptable to add a new class to the public API.


.. data:: popjava.annotation.✳

    The minimum needed to use POP. This package contains all the ``@POP`` annotations for methods and classes.

.. note :: We have 6 different sending methods, but generally if we want to add an option we add it to the six of them.


.. data:: popjava.PopJava

    Needed for some POP specific tasks like getting the Access Point of a POP Object.

    It contains all methods to initiate a new POP Object.


.. data:: popjava.JobManagerConfig

    Used to configure the JobManager on the local machine. It's a Proxy to methods of ``POPJavaJobManager``.


.. data:: popjava.util.Configuration

    If the user need some more control on the behavior of POP-Java. Controls include timeouts and the defaults
    used in various situations.


.. data:: popjava.baseobject.ConnectionProtocol

    Used by ``@POPObjectDescription(connection = ...)`` to define the direct method of connection used.
