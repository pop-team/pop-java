.. _user-configuration:
Configuration
=============

After installation of POP-Java we need to take some steps if we want to enable some advanced features.

To do this a small dedicated shell was created. To run it go into the POP installation directory and run:

.. code-block:: txt

    $ java -javaagent:JarFile/popjava.jar -cp JarFile/popjava.jar popjava.scripts.POPJShell
    This shell is not interactive, you must type every command.
    Use ``help`` to know the available commands.
    Every command has a --help (-h) flag which print its options.
    $

This will open the shell. To execute a command simply write it and press enter. No history is available at this time.

.. code-block:: txt

    $ help
    Available options:
      help                  print this help
      jm                    configuration of the local job manager
      debug                 toggle system debug option
      keystore              all keystore related operations.

About the Shell
~~~~~~~~~~~~~~~

Every command in the shell has a ``help`` method, usually by adding ``-h`` or ``--help`` to it. When it asks for a missing value is because that value could have been given by an option. See below.

.. code-block:: txt

    $ jm node add -h
    usage: jm node add [OPTIONS]
    add a new node to a network
    Available options:
      --type, -t          The type of node we are working with (jobmanager, tfc, direct)
      --uuid, -u          The UUID of the network to add the node into
      --host, -H          The destination host of the node
      --port, -p          The destination port of the node
      --protocol, -P      The node specific protocol (socket, ssl, daemon)
      --certificate, -c   The certificate for the SSL connection
    Node specific options will be asked.



TLS Configuration
-----------------

In case you want to use secure connections, you first have to create a keystore.
Using the ``keystore create`` command the command will ask us to insert all the needed values. It will also save the keystore information in the POP-Java's global configuration so users will be able to use secure connection too.

.. code-block:: txt

    $ keystore create
    missing value for 'file': global.jks
    missing value for 'storepass':
    missing value for 'keypass':
    missing value for 'alias': localNodeOnly
    missing value for 'rdn': OU=PopJava,CN=testNode
    Generating keystore...
    Saving configuration...

Job Manager Configuration
-------------------------

If there is not a third party application to configure the Job Manager, the shell also partially give this capability.

The first thing to do is start the Job Manager.

.. code-block:: txt

    $ jm start
    Job Manager started.

With this we can now interact with it.

Network creation
~~~~~~~~~~~~~~~~

To create a new network you will have to execute the ``jm network create`` command. Its output should something like the folowing snippet.

.. code-block:: txt

    $ jm network create
    missing value for 'name': friendly net
    missing value for 'uuid':
    Network 'friendly net' created with id [d3fe0096-e582-4b85-bdc0-a429b169d24f]
    Network certificate available at '/home/dosky/pop-java-dist/friendly net@d3fe0096-e582-4b85-bdc0-a429b169d24f.cer'

The command will also export a ``.cer`` file which can be shared with trusted parties to communicate with them.

.. note::

    The UUID value is what really identify the network, if someone else want to communicate with you it has to create a network matching the generated UUID in the command above.
    This means not leaving it blank.

You can see the existing network by running ``jm network list``

.. code-block:: txt

    $ jm network list
    Note that networks are identified by their UUID.
    +------------------------------------------+--------------------------------+
    | UUID                                     | Friendly name                  |
    +==========================================+================================+
    | d3fe0096-e582-4b85-bdc0-a429b169d24f     | friendly net                   |
    +------------------------------------------+--------------------------------+

Adding friendly nodes
~~~~~~~~~~~~~~~~~~~~~

Similarly to how we add network, a command exists in order to add friendly nodes.

.. code-block:: txt

    $ jm node add
    missing value for 'type': jobmanager
    missing value for 'uuid': d3fe0096-e582-4b85-bdc0-a429b169d24f
    missing value for 'host': <host>
    missing value for 'port': <port>
    missing value for 'protocol': ssl
    missing value for 'certificate': other certificate.cer
    Node added to network 'd3fe0096-e582-4b85-bdc0-a429b169d24f'

.. note::

    Currently there exists three ``type`` of node: tfc, jobmanager, direct.

    Currently there exists two ``protocol``: socket, ssl.

    When working with ``ssl`` a certificate is needed and the connection will be encrypted, while ``socket`` will be unencrypted.

Executing object as another user
--------------------------------

Generally speaking the Job Manager on a machine has access to sensitive information like the content of the keystore. We don't want anyone except the system administrator to be able to modify those files.

Other options
-------------

POP-Java is very flexible, most of its options can be user configurable.

The shell by itself doesn't give the possibility of setting most of those options, bu they can be manually modified by adding the keyword and the value in the ``popjava.properties`` file situated in the ``etc`` directory of the POP installation.

A use can potentially modify those option for its own application by adding a ``-configfile=<file>`` option at the program execution.

For more information in regards of the options, check the ``popjava.util.Configuration`` class in the Javadoc or the developer Configuration section.