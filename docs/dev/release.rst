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

#. Update version 

	Increment the version number of POP-Java in the ``build.gradle`` file. This step is mandatory for a new Maven release.	


#. Update author file

	All current and/or previous authors (*core committers*) shall be listed in the ``AUTHORS`` file as follows:
	 
	.. literalinclude:: ../../AUTHORS


#. Build Jar
	
	The version of POP-Java to be released must be built locally by using the following command::

  		$ ./gradlew fatJar
  		
  	.. note:: Make sure you use Java 8 (not 9) to build POP-Java. Otherwise it will not run under Java 8.


#. Run tests locally

	POP-Java must be tested locally by using the following command::

  		$ ./gradlew test
	
	All bugs found must be fixed until all tests have passed.

	
#. Build and upload Maven package to OSSRH 

	Build the POP-Java Jar files and signing files required for the Maven package, and upload (deploy) them to `OSSRH  <https://oss.sonatype.org>`_ repository by using the following commands::

  		$ ./gradlew clean
  		$ ./gradlew uploadArchives  		
  	
  	.. note:: 
  		* We first clean the build directory to get rid of the fat Jar bundle, which must not be deployed to the OSSRH repository. 
  		
  		* To perform this step, one must have a `Sonatype JIRA login <https://issues.sonatype.org>`_ and `credentials <http://central.sonatype.org/pages/gradle.html>`_ in his gradle.properties file (generally stored in ~/.gradle/) like this::
  	
  		 	signing.keyId=YourKeyId
  	  	 	signing.password=YourPublicKeyPassword
  	  	 	signing.secretKeyRingFile=PathToYourKeyRingFile
  	  	 	
  	  	 	ossrhUsername=your-jira-id
  	  	 	ossrhPassword=your-jira-password
  	  	 
  	 	* The signing data must be generated, e.g. with `GnuPG <http://central.sonatype.org/pages/working-with-pgp-signatures.html>`_ .  
  	  	

#. Commit, tag and push

	Commit your changes to the project, tag your version and push them::
	
	$ git commit -m "My commit message"
	$ git tag -a v2.1.0 -m "my version 2.1.0"
	$ git push origin master
	$ git push --tags
	

#. Wait for tests to pass and documentation to build

	Here nothing to do but wait. While one or more tests fail, please fix the related bugs and go back to previous step.


#. Update release details on GitHub

	Please follow these steps:
	
	#. Go to the `GitHub release page <https://github.com/pop-team/pop-java/releases>`_;
	#. Click on the new release link;
	#. Click on the ``Edit tag`` button (on the top right of the page);
	#. Fill in the related fields;
	#. Click on the ``Publish release`` button.


#. Release deployed Maven package from OSSRH to the Central Repository

	Automatically close and release the staging version from `OSSRH  <https://oss.sonatype.org>`_ to the `Central Repository <https://search.maven.org>`_ by using the following command::

  		./gradlew closeAndReleaseRepository
  		
  	.. note:: To pass this step, the deployed files are verified and thus must fulfil the `requirements <http://central.sonatype.org/pages/requirements.html>`_
	
