Release management
==================

Creation of a new release
-------------------------

.. todo:: This is a draft.

#. Update changelog file

	All notable changes to the POP-Java project will be documented in the ``CHANGELOG.md`` file as follows (source: `SemVer <https://semver.org>`_). Given a version number **MAJOR.MINOR.PATCH**, increment the:

	* **MAJOR version**, when incompatible API changes are made;
	* **MINOR version**, when functionalities in a backwards-compatible manner are added;
	* **PATCH version**, when backwards-compatible bug fixes are made.
	
	Additional labels for pre-release (e.g. Beta, RC1) and build metadata may be added as extensions to the MAJOR.MINOR.PATCH format.

	For each new version released, the related section will list its novelties under the following potential sub-sections: Features, Bug Fixes and BREAKING CHANGES. 

	New functionalities, which are not yet released, will be listed at the top of the CHANGELOG.md under the so-called UNRELEASED section.


#. Update author file

	All current and/or previous authors (*core committers*) shall be listed in the ``AUTHORS`` file as follows:
	 
	.. literalinclude:: ../../AUTHORS


#. Build Jar
	
	The version of POP-Java to be released must be built locally by using the following command::

  		$ ./gradlew build -x test

#. Run tests locally

	POP-Java must be tested locally by using the following command::

  		$ ./gradlew test
	
	All bugs found must be fixed until all tests have passed.
	
	.. note:: This step and the preceding one can be executed both at once by using the following command::

		$ ./gradlew build 

#. Build Maven package 

	.. todo:: In progress...

#. Commit, tag and push

	Commit your changes to the project, tag your version and push them::
	
	$ git commit -m "My commit message"
	$ git tag -a v2.1.0 -m "my version 2.1.0"
	$ git push origin master
	$ git push --tags
	
#. Wait for tests to pass and documentation to build

	Here nothing to do but wait.

#. Update release details on GitHub

	Please follow these steps:
	
	#. Go to the `GitHub release page <https://github.com/pop-team/pop-java/releases>`_;
	#. Click on the new release link;
	#. Click on the ``Edit tag`` button (on the top right of the page);
	#. Fill in the related fields;
	#. Click on the ``Publish release`` button.

#. Publish Maven package

	.. todo:: In progress... (attach package https://github.com/pop-team/pop-java/releases/)
	
