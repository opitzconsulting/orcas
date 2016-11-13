Notes for Maven Central Sync
============================

This project is using [Sonatype OSS](https://oss.sonatype.org/) to publish Orcas libraries to [Maven Central Repository](http://repo1.maven.org/maven2).

Because our project is build with Gradle we needed some
We used the following blog post to set up our Gradle build:
http://mike-neck.github.io/blog/2013/06/21/how-to-publish-artifacts-with-gradle-maven-publish-plugin-version-1-dot-6/

New Deployer
------------

Every developer who wants to deploy to Sonatype staging must meet the following requirements:

* Create Account at Sonatype JIRA https://issues.sonatype.org/secure/Signup!default.jspa
* Associate the newly created account with our project (We do not know how to do yet!)
* Create PGP signatures according to http://central.sonatype.org/pages/working-with-pgp-signatures.html
  * Warning: Make sure to delete all [Sub Keys](http://central.sonatype.org/pages/working-with-pgp-signatures.html#delete-a-sub-key) to sign artifacts with your primary key
* Set up local configuration file at {USER_DIR}/.gradle/gradle.properties with the following information:

    # GPG signing
    signing.keyId=C9B43993
    signing.password=**********
    signing.secretKeyRingFile=/Users/{USER_DIR}/.gnupg/secring.gpg

    # Sonatype
    sonatypeUsername=codescape
    sonatypePassword=*********

Deployment
----------

At the moment we need to issue the following command chain to deploy into the staging environment:

    gradle clean generatePomFileForMavenJavaPublication pomExtremeMover preparePublication publish

At first we clean up temporary files with `clean` and generate the Maven POM files from the Gradle configuration with `generatePomFileForMavenJavaPublication`. These files are then renamed to meet the correct naming for Maven by `pomExtremeMover`. Finally the publication is prepared and performed by `preparePublication` and `publish`.


https://oss.sonatype.org/

