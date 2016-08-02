POP-Java 1.0
===========

Version 1.0 is under development, use it at your own risk.

COMPILE POP-Java SOURCE CODE
---------------------------

.. code::

  user@computer$ ant


INSTALLING POP-C++
------------------

.. code::

  user@computer$ ./install

You might use sudo or to be root tu execute "make install" if you do not have the right to write in the destination folder.

The installation script will ask you several questions. Here they are:

1. Please enter the location of your desired POP-Java installation (default: /usr/local/popj ) :

If unsure leave the answer blank to keep the default setting.

STARTING POP-C++
----------------

**Do not forget to add these lines to your .bashrc file or equivalent :**
If you have kept the default options this should give you something like:

.. code::
  
  POPJAVA_LOCATION=/usr/local/popj
  export POPJAVA_LOCATION
  POPJAVA_JAVA=/usr/bin/java
  export POPJAVA_JAVA
  PATH=$PATH:$POPJAVA_LOCATION/bin

RESOURCES
---------
* The user manual `<http://pop-java.readthedocs.io/en/latest/>`_
