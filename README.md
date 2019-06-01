firebird-testcontainers-java
============================

Firebird-testcontainers-java is a module for [Testcontainers](https://www.testcontainers.org/)
to provide lightweight, throwaway instances of Firebird for JUnit tests.

The docker image used is [jacobalberty/firebird](https://hub.docker.com/r/jacobalberty/firebird/).

Prerequisites
-------------

- Docker
- A supported JVM testing framework

See [Testcontainers prerequisites](https://www.testcontainers.org/#prerequisites) for details.

Dependency
----------

**NOTE: Dependency is not yet deployed to Maven Central**

### Gradle

```groovy
testCompile "org.firebirdsql:firebird-testcontainers-java:1.0.0"
```

### Maven

```xml
<dependency>
    <groupId>org.firebirdsqls</groupId>
    <artifactId>firebird-testcontainers-java</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

License
-------

See [LICENSE](LICENSE)
