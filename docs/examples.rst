Examples
========

The POP-Java distribution includes some examples of POP-Java application. These
examples can be found in the folder ``POPJAVA_DISTRIBUTION/example``. All
examples have a ``Makefile`` to compile and a special target ``run`` to run
them. The following examples are available for the moment:

* ``Callback``: this example shows the ability of a parallel object to
  call back the one that called him.
* ``Integer``: this is a simple example of a parallel object integer
  (same as the example in POP-C++).
* ``Mixed1``: this example is a POP-Java application using a POP-C++
  integer parallel object
* ``Mixed2``: this example is a POP-C++ application using a POP-Java
  integer parallel object.
* ``Multiobj``: this example shows a chaining of parallel object.
