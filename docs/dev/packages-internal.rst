
Internal packages and classes
=============================

This section explain the most important internal classes used by the POP Runtime.

.. note:: We are not going to describe every class in this section, only the most important which may need further
    explanation.


When modifying POP's internal classes be sure to run the JUnit tests before and after and ensure that no new error are generated from the modification.
If what is added is designed to fail in some scenarios it is advised to add a new test to JUnit, to see how to do this see :doc:`testing`.



.. _popjava.PJProxyFactory:
.. data:: popjava.PJProxyFactory

    Use `Javaassist <https://jboss-javassist.github.io/javassist/>`_ to wrap a class into a POP Object.
    In ``newPOPObject`` it will create a new ``PJMethodHandler`` which will also create the new JVM for the POP Object.

.. _popjava.PJMethodFilter:
.. data:: popjava.PJMethodFilter

    A helper class to knew which method are to be handled by ``PJMethodHandler``, it also contains a *static* set of special POP methods which are to be handled internally.

.. _popjava.PJMethodHandler:
.. data:: popjava.PJMethodHandler

    Extends ``Interface`` and add the ability of calling methods. The methods in the special set in :ref:`PJMethodFilter <popjava.PJMethodFilter>` are implemented here.

.. _popjava.annotation.processors.POPClassProcessor:
.. data:: popjava.annotation.processors.POPClassProcessor

    `` ``

.. _popjava.base.POPObject:
.. data:: popjava.base.POPObject

    `` ``

.. _popjava.base.POPErrorCode:
.. data:: popjava.base.POPErrorCode

    `` ``

.. _popjava.baseobject.ObjectDescription:
.. data:: popjava.baseobject.ObjectDescription

    `` ``

.. _popjava.baseobject.AccessPoint:
.. data:: popjava.baseobject.AccessPoint

    `` ``

.. _popjava.baseobject.POPAccessPoint:
.. data:: popjava.baseobject.POPAccessPoint

    `` ``

.. _popjava.broker.Broker:
.. data:: popjava.broker.Broker

    `` ``

.. _popjava.broker.Request:
.. data:: popjava.broker.Request

    `` ``

.. _popjava.buffer.POPBuffer:
.. data:: popjava.buffer.POPBuffer

    `` ``

.. _popjava.buffer.BufferXDR:
.. data:: popjava.buffer.BufferXDR

    `` ``

.. _popjava.buffer.BufferRaw:
.. data:: popjava.buffer.BufferRaw

    `` ``

.. data:: abc

    `` ``

.. data:: def

    `` ``
.. todo:: Continue adding and write descriptions


