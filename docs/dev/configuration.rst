Configuration
=============

POP-Java can be configured by placing a file in a specific location: ``${POPJAVA_LOCATION}/etc/popjava.properties``.

There exists three level of configuration in POP-Java, the first level is POP Hardcoded values, the second is system level override in the location specified above, while the third level is user level override which enable the user to tweak POP even more locally. This can be seen in :num:`popconf`.

.. _popconf:
.. figure:: ../images/popj-configuration-layers.png

    POP-Java Configuration Layers

.. note:: For testing purpose the path ``./etc/popjava.properties`` is also valid.

Parameters available
--------------------

.. todo:: describe data

.. _SYSTEM_JOBMANAGER_CONFIG:
.. data:: SYSTEM_JOBMANAGER_CONFIG : File

    `` ``

.. _DEBUG:
.. data:: DEBUG : Booelan

    `` ``

.. _DEBUG_COMBOBOX:
.. data:: DEBUG_COMBOBOX : Booelan

    `` ``

.. _RESERVE_TIMEOUT:
.. data:: RESERVE_TIMEOUT : Int

    `` ``

.. _ALLOC_TIMEOUT:
.. data:: ALLOC_TIMEOUT : Int

    `` ``

.. _CONNECTION_TIMEOUT:
.. data:: CONNECTION_TIMEOUT : Int

    `` ``

.. _JOBMANAGER_UPDATE_INTERVAL:
.. data:: JOBMANAGER_UPDATE_INTERVAL : Int

    `` ``

.. _JOBMANAGER_SELF_REGISTER_INTERVAL:
.. data:: JOBMANAGER_SELF_REGISTER_INTERVAL : Int

    `` ``

.. _JOBMANAGER_DEFAULT_CONNECTOR:
.. data:: JOBMANAGER_DEFAULT_CONNECTOR : String

    `` ``

.. _JOBMANAGER_PROTOCOLS:
.. data:: JOBMANAGER_PROTOCOLS : String[]

    `` ``

.. _JOBMANAGER_PORTS:
.. data:: JOBMANAGER_PORTS : Int[]

    `` ``

.. _JOBMANAGER_EXECUTION_BASE_DIRECTORY:
.. data:: JOBMANAGER_EXECUTION_BASE_DIRECTORY : File

    `` ``

.. _JOBMANAGER_EXECUTION_USER:
.. data:: JOBMANAGER_EXECUTION_USER : String

    `` ``

.. _POP_JAVA_DEAMON_PORT:
.. data:: POP_JAVA_DEAMON_PORT : Int

    `` ``

.. _SEARCH_NODE_UNLOCK_TIMEOUT:
.. data:: SEARCH_NODE_UNLOCK_TIMEOUT : Int

    `` ``

.. _SEARCH_NODE_SEARCH_TIMEOUT:
.. data:: SEARCH_NODE_SEARCH_TIMEOUT : Int

    `` ``

.. _SEARCH_NODE_MAX_REQUESTS:
.. data:: SEARCH_NODE_MAX_REQUESTS : Int

    `` ``

.. _SEARCH_NODE_EXPLORATION_QUEUE_SIZE:
.. data:: SEARCH_NODE_EXPLORATION_QUEUE_SIZE : Int

    `` ``

.. _TFC_SEARCH_TIMEOUT:
.. data:: TFC_SEARCH_TIMEOUT : Int

    `` ``

.. _DEFAULT_ENCODING:
.. data:: DEFAULT_ENCODING : String

    `` ``

.. _SELECTED_ENCODING:
.. data:: SELECTED_ENCODING : String

    `` ``

.. _DEFAULT_PROTOCOL:
.. data:: DEFAULT_PROTOCOL : String

    ``socket``

.. _PROTOCOLS_WHITELIST:
.. data:: PROTOCOLS_WHITELIST : Set<String>

    `` ``

.. _PROTOCOLS_BLACKLIST:
.. data:: PROTOCOLS_BLACKLIST : Set<String>

    `` ``

.. _ASYNC_CONSTRUCTOR:
.. data:: ASYNC_CONSTRUCTOR : Booelan

    `` ``

.. _ACTIVATE_JMX:
.. data:: ACTIVATE_JMX : Booelan

    `` ``

.. _CONNECT_TO_POPCPP:
.. data:: CONNECT_TO_POPCPP : Booelan

    `` ``

.. _CONNECT_TO_JAVA_JOBMANAGER:
.. data:: CONNECT_TO_JAVA_JOBMANAGER : Booelan

    `` ``

.. _REDIRECT_OUTPUT_TO_ROOT:
.. data:: REDIRECT_OUTPUT_TO_ROOT : Booelan

    `` ``

.. _USE_NATIVE_SSH_IF_POSSIBLE:
.. data:: USE_NATIVE_SSH_IF_POSSIBLE : Booelan

    `` ``

.. _SSL_PROTOCOL_VERSION:
.. data:: SSL_PROTOCOL_VERSION : String

    `` ``

.. _SSL_KEY_STORE_FILE:
.. data:: SSL_KEY_STORE_FILE : File

    `` ``

.. _SSL_KEY_STORE_PASSWORD:
.. data:: SSL_KEY_STORE_PASSWORD : String

    `` ``

.. _SSL_KEY_STORE_PRIVATE_KEY_PASSWORD:
.. data:: SSL_KEY_STORE_PRIVATE_KEY_PASSWORD : String

    `` ``

.. _SSL_KEY_STORE_LOCAL_ALIAS:
.. data:: SSL_KEY_STORE_LOCAL_ALIAS : String

    `` ``

.. _SSL_KEY_STORE_FORMAT:
.. data:: SSL_KEY_STORE_FORMAT : KeyStoreFormat

    `` ``



New attribute
-------------

Adding a new attribute require the modification of the Configuration class, this is because we grant access to attributes via ``get`` and ``set`` methods.
The process is done 4 steps.

1. Choose the name of the attribute and add it to the ``Settable`` enumerator. ::

    private enum Settable {
        MY_NEW_ATTRIBUTE,
        ...
    }

2. Add a class attribute which will be used to store the value. ::

    private String myNewAttribute = "";

3. Create getter and setter methods. ::

    public String getMyNewAttribute() {
        return myNewAttribute;
    }
    public void setMyNewAttribute(String value) {
        USER_PROPERTIES.setProperty(Settable.MY_NEW_ATTRIBUTE.name(), value);
        myNewAttribute = value;
    }

.. note:: Using ``USER_PROPERTIES`` enable us to save only the changed information if the User call ``store()``.

4. Add the parsing rules in ``load``. ::

    switch(keyEnum) {
        case MY_NEW_ATTRIBUTE: myNewAttribute = value; break;
        ...
    }


Remarks
-------

Java all Java version except Java 9, properties file are encoded with ISO-8859-1 which means that all character outside the first 256 byte will be encoded with its hexadecimal form ``\uXXXX``.
For this reason be on alert when using characters outside this charset manually.
From Java 9 properties files are saved using UTF-8 so this problem shouldn't matter.
