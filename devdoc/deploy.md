Deploying
=========

Make sure that JAVA_HOME points to Java 8 (the javadoc task fails with Java 11).

To deploy to Maven use

```
mvn clean deploy -P release
```

For snapshots, we can forego signing and generating javadoc + sources using:

```
mvn clean deploy
```

This requires the proper Sonatype credentials to be set in userhome/.m2/settings.xml.

See https://central.sonatype.org/pages/apache-maven.html for details.

Updating version
----------------

```
mvn versions:set -DnewVersion=<new version>
```
