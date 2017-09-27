Configuration
=============

POP-Java can be configured by placing a file in a specific location: ``${POPJAVA_LOCATION}/etc/popjava.properties``.

There exists three level of configuration in POP-Java, the first level is POP Hardcoded values, the second is system
level override in the location specified above, while the third level is user level override which enable the user to
tweak POP even more locally. This can be seen in :num:`popconf`.

.. _popconf:
.. figure:: ../images/popj-configuration-layers.png

    POP-Java Configuration Layers

.. note:: For testing purpose the path ``./etc/popjava.properties`` is also valid.

Parameters available
--------------------

.. todo:: describe data

.. data:: SYSTEM_JOBMANAGER_CONFIG : File



.. data:: DEBUG : Booelan



.. data:: DEBUG_COMBOBOX : Booelan



.. data:: RESERVE_TIMEOUT : Int



.. data:: ALLOC_TIMEOUT : Int



.. data:: CONNECTION_TIMEOUT : Int



.. data:: JOBMANAGER_UPDATE_INTERVAL : Int



.. data:: JOBMANAGER_SELF_REGISTER_INTERVAL : Int



.. data:: JOBMANAGER_DEFAULT_CONNECTOR : String



.. data:: JOBMANAGER_PROTOCOLS : String[]



.. data:: JOBMANAGER_PORTS : Int[]



.. data:: JOBMANAGER_EXECUTION_BASE_DIRECTORY : File



.. data:: JOBMANAGER_EXECUTION_USER : String



.. data:: POP_JAVA_DEAMON_PORT : Int



.. data:: SEARCH_NODE_UNLOCK_TIMEOUT : Int



.. data:: SEARCH_NODE_SEARCH_TIMEOUT : Int



.. data:: SEARCH_NODE_MAX_REQUESTS : Int



.. data:: SEARCH_NODE_EXPLORATION_QUEUE_SIZE : Int



.. data:: TFC_SEARCH_TIMEOUT : Int



.. data:: DEFAULT_ENCODING : String



.. data:: SELECTED_ENCODING : String



.. data:: DEFAULT_PROTOCOL : String



.. data:: PROTOCOLS_WHITELIST : Set<String>



.. data:: PROTOCOLS_BLACKLIST : Set<String>



.. data:: ASYNC_CONSTRUCTOR : Booelan



.. data:: ACTIVATE_JMX : Booelan



.. data:: CONNECT_TO_POPCPP : Booelan



.. data:: CONNECT_TO_JAVA_JOBMANAGER : Booelan



.. data:: REDIRECT_OUTPUT_TO_ROOT : Booelan



.. data:: USE_NATIVE_SSH_IF_POSSIBLE : Booelan



.. data:: SSL_PROTOCOL_VERSION : String



.. data:: SSL_KEY_STORE_FILE : File



.. data:: SSL_KEY_STORE_PASSWORD : String



.. data:: SSL_KEY_STORE_PRIVATE_KEY_PASSWORD : String



.. data:: SSL_KEY_STORE_LOCAL_ALIAS : String



.. data:: SSL_KEY_STORE_FORMAT : KeyStoreFormat





New attribute
-------------

Adding a new attribute require the modification of the Configuration class, this is because we grant access to
attributes via ``get`` and ``set`` methods.
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

Java all Java version except Java 9, properties file are encoded with ISO-8859-1 which means that all character outside
the first 256 byte will be encoded with its hexadecimal form ``\uXXXX``.
For this reason be on alert when using characters outside this charset manually.
From Java 9 properties files are saved using UTF-8 so this problem shouldn't matter.