Basic example
=============


In this section, we explain how to publish an object and how to retrieve one inside a friend network.


Publishing TFC objects
----------------------

TFC requires that a connected *friend* shares a known application consisting of a POP object.

Let's take the following class as an example:

.. code-block:: java

	@POPClass
	public class A {

		private int n;

		public A() { }

		@POPObjectDescription(url = "localhost", protocols = "ssl")
		public A(int n) {
			this.n = n;
		}

		@POPSyncConc
		public int get() {
			return n;
		}
	}

The class ``A`` is very basic. It only offers one method to retrieve a stored value *n*.

The following application shows how to publish an instance of ``A`` to our network:

.. code-block:: java
	:emphasize-lines: 5,9-10

	@POPClass(isDistributable = false)
	public class TFCPublish {
		private static final String NET = "abc-123-def";
		public static void main(String[] args) throws IOException {
			A a = new A();
			System.out.println("Object at " + PopJava.getAccessPoint(a));

			System.out.println("Publishing via JMC...");
			JobManagerConfig jmc = new JobManagerConfig();
			jmc.publishTFCObject(a, NET, "mysecret");

			System.out.println("Press enter to destroy the object...");
			System.in.read();
		}
	}

At line ``5``, we can see a standard POP object creation.
At line ``9``, we connect to the Job Manager using its configuration API class.
At line ``10``, we publish the created object by giving it to the Job Manager and setting which network should be able to search for the object. The ``secret`` allows to remove the object from the published list without killing it.

.. note:: We took an unique network identifier ``abc-123-def`` for sake of simplicity. See :ref:`user-configuration` to learn how to create networks.


Searching for a TFC object
--------------------------

To search for an object, we require to know its interface (and not necessarily its implementation). In our example, we have its implementation.

Note that friend nodes may be offline at the time of search, meaning that multiple searchs for the same object might yield different results.

Retrieving objects require the following steps:
	1. Setup of the search parameters (lines ``6,7``);
	2. Initialization of the search (line ``8``);
	3. Connection to the obtained results (line ``12``).

.. code-block:: java
	:emphasize-lines: 6-8,12

	@POPClass(isDistributable = false)
	public class TFCRetrieve {
		private static final String NET = "abc-123-def";
		public static void main(String[] args) {
			System.out.println("Retrieving from network...");
			ObjectDescription od = new ObjectDescription();
			od.setNetwork(NET);
			POPAccessPoint[] aps = PopJava.newTFCSearch(A.class, 10, od);
			System.out.println("Got " + Arrays.toString(aps));

			for (POPAccessPoint ap : aps) {
				A r = PopJava.connect(A.class, NET, ap);
				System.out.println(ap + " -> " + r.get());
			}
		}
	}

The *ObjectDescriptor* at line ``6,7`` sets the network in which we will search for objects. There we can also specify some options such as ``setSearch`` for the depth of the search, or ``setSearchHosts`` to select the hosts which are allowed to answer us from the host list.

The API call to ``PopJava.newTFCSearch`` requires the class we are looking for, the maximum number of instances we want and the *ObjectDescriptor* as parameters.

In order to connect to an existing object, we use the ``PopJava.connect`` method which requires the network, the remote object and its address.

.. note:: The current way to publish and retrieve objects require several API calls, which is not in the best spirit of a POP model (KISS). This might be simplified in the future.

