Advanced example
================

In this chapter we will see a more complex example of the use of TFC. In the example we don't have defaults like the network, we will publish only partially on the available networks.

We will also some advanced annotations which can be used in more complex applications such as ``@POPPrivate`` and the parameters ``localhost``, ``tracking`` and ``localJVM``.

.. code-block:: java
	:emphasize-lines: 9,19,25

	@POPClass
	public static class A {

		private int n;

		public A() { }

		@POPObjectDescription(url = "localhost", protocols = "ssl",
			tracking = true, localJVM = true)
		public A(int n) {
			this.n = n;
		}

		@POPSyncSeq
		public int get() {
			return n;
		}

		@POPAsyncMutex(localhost = true)
		public void set(int n) {
			this.n = n;
		}

		@POPSyncSeq
		@POPPrivate
		public void divide() {
			n /= 2;
		}
	}

The class above does the same thing as the one previously presented, it expose a value on to whoever want to know it. The main difference is that, this time, we have the ability to modify the value even after the object creation. **But this ability is not for everyone.**

Local JVM objects
-----------------

We can see in the ``@POPObjectDescription`` annotation that we have two new attributes: ``tracking`` which will allow us to know who used the object and ``localJVM`` which will not spawn a new JVM for this object but will integrate it in the current one, see :num:`localjvm-ex`

.. _localjvm-ex:
.. figure:: ../images/localjvm-schema.png

	Main create two POP objects, one is a ``localJVM`` one, the other is a classic one.

Why create an object this way?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

There are multiple reasons, the main one is that the POP object spawned locally doesn't require data to passed via a Combox and has access to all the non-POP objects created in the JVM. It is usually used when accessing data from a non-POP platform and make it available to a POP application.

Not an all in one solution
~~~~~~~~~~~~~~~~~~~~~~~~~~

Note that ``localJVM`` should be used with care, annotation used to achieve synchronicity may not work (particularly ``async``) unless we treat the object as if it was a remote one.

.. code-block:: java

	A al = new A(10); // local JVM
	A ar = PopJava.getThis(al); // connect to the local JVM object

In the example above ``al`` is a ``localJVM`` object and is treated as such, on the other hand ``ar`` while also pointing to the same object ``al`` it will have to pass through a ``Combox`` to make calls, it also lose access to some methods.

Local special access method
---------------------------

We use ``localJVM`` when we are making an hybrid application which will work with non-POP objects, for this reason it is possible that some of the object methods shouldn't be available to every connecting client but only to the JVM which created the object itself.

We use the ``@POPPrivate`` annotation to not expose remotely a method, but this will stay available to the JVM which created the object.

.. code-block:: java

	// node A
	A local = new A(10); // local JVM
	A ref1 = PopJava.getThis(local); // connect to the local JVM object

	// node B
	A ref2 = PopJava.connect(...) // connect to nodeA -> local remotely

Let's take the above snippet as an example, we create a ``localJVM`` object, connect to it creating a reference and also have a remote machine also connect to it. Visually it would resemble the :num:`localjvm-situation`.

.. _localjvm-situation:
.. figure:: ../images/localjvm-situation.png

	Local JVM with local and remote connections

In this example only ``local`` will be able to call the method ``divide``, ``ref1`` and ``ref2`` will not have this method exposed because it is marked a ``POPPrivate``.

Remote special access method
----------------------------

``@POPPrivate`` is not the only restriction we can make, notice how the ``set`` method has an attribute in its annotation, ``localhost = true``. This attribute automatically check that the calls to this method are coming from someone on the same machine of the object.

Using the same example as previous one we can see that the ``set`` method is not accessible by everyone, but only by the objects on Node A. The table below shows the access to the three methods of ``A``.

+------------+-------+-------+-------+
|   Method   | local | ref1  | ref2  |
+============+=======+=======+=======+
| **get**    |   ✔   |   ✔   |   ✔   |
+------------+-------+-------+-------+
| **set**    |   ✔   |   ✔   |   ✖   |
+------------+-------+-------+-------+
| **divide** |   ✔   |   ✖   |   ✖   |
+------------+-------+-------+-------+

Tracking
--------

Tracking let us know who and how long an object was used. To know this information we require calls to specialized API with a POP object as a target.

We will use the same example used in the two last chapters, one object receive two connections from two different sources and is also used locally.

.. note::

	It's important to know that we can't track the usage of a ``localJVM`` object unless we are connected to it via a Combox. This means we will never know how ``local`` used A.

Statistics
~~~~~~~~~~

Whoever created the object has the most access to the usage statistics of a POP object, in fact only the owner can know all the user who used the object.

The information we can extract from a connecting user are various: normally it collect the IP address used to connect, the certificate (if present) used to identify itself and the network used for the connection.

.. note::

	Notice how POP-Java doesn't handle the real identification of a user, it's the job of the one creating an application to provide this capability.

To access the statistics of a POP object we use the APIs provided by ``POPAccounting``. This class let us do principally 3 things:
	- See if an object has tracking enabled
	- Retrieve the users which used the object
	- Ask the stats for a given user
	- Ask the stats about the current connection

Own statistics
~~~~~~~~~~~~~~

Access your own statistics can be useful if you want to check how much you used another person shared object before closing a connection, usage is stacked and not connection independent. This means that if you connect two time to an object the the second time you request your statistics it will also contains the ones from the first connection.

.. code-block:: java

	POPTracking own = POPAccounting.getMyInformation(a);

``POPTracking`` contains the information the owner of the object will be able to see about yourself to identify you and your usage of the methods in the object.

Object statistics
~~~~~~~~~~~~~~~~~

If you are the owner of an object you are interested in knowing who used your object, for doing so you first need to request a user list to the object and successively ask detailed information on each of the user.

.. code-block:: java

	POPRemoteCaller[] users = POPAccounting.getUsers(a);
	for (POPRemoteCaller user : users) {
		POPTracking info = POPAccounting.getInformation(a, user);
		// do something
	}

Tracked information
~~~~~~~~~~~~~~~~~~~

We generally track three things the user did:
	- the method he used
	- how many times did he call said method
	- how long did the method execute for

With those information an owner of an object should be able to gather enough information to fill an invoice.