firebird-testcontainers-java
============================

[![Build Status](https://travis-ci.com/FirebirdSQL/firebird-testcontainers-java.svg?branch=master)](https://travis-ci.com/FirebirdSQL/firebird-testcontainers-java)
[![MavenCentral](https://maven-badges.herokuapp.com/maven-central/org.firebirdsql/firebird-testcontainers-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.firebirdsql/firebird-testcontainers-java/)

Firebird-testcontainers-java is a module for [Testcontainers](https://www.testcontainers.org/)
to provide lightweight, throwaway instances of Firebird for JUnit tests.

The docker image used is [jacobalberty/firebird](https://hub.docker.com/r/jacobalberty/firebird/).

If you want to use 2.5, use the 2.5.x-sc (SuperClassic) variant of the image, or 2.5.9-ss
as earlier versions of the 2.5.x-ss (SuperServer) variant seem to be broken.

Prerequisites
-------------

- Docker
- A supported JVM testing framework

See [Testcontainers prerequisites](https://www.testcontainers.org/#prerequisites) for details.

Dependency
----------

In addition to the firebird-testcontainers-java dependency, you will also need
to explicitly depend on [Jaybird](https://github.com/FirebirdSQL/jaybird).

### Gradle

```groovy
testImplementation "org.firebirdsql:firebird-testcontainers-java:1.1.0"
```

### Maven

```xml
<dependency>
    <groupId>org.firebirdsql</groupId>
    <artifactId>firebird-testcontainers-java</artifactId>
    <version>1.1.0</version>
    <scope>test</scope>
</dependency>
```

Usage
-----

For extensive documentation, consult https://www.testcontainers.org/modules/databases/

### JUnit rule

Using a JUnit `@Rule` or `@ClassRule` you can configure a container and start it
per test (`@Rule`) or per class (`@ClassRule`).

The container defines several `withXXX` methods for configuration.

Important standard options are:

- `withUsername(String)` - Sets the username to create (defaults to `test`); if
other than `sysdba` (case-insensitive), sets docker environment variable
`FIREBIRD_USER`
- `withPassword(String)` - Sets the password of the user (defaults to `test`);
if `withUsername` is other than `sysdba` (case-insensitive), sets the docker
environment variable `FIREBIRD_PASSWORD`, otherwise `ISC_PASSWORD`
- `withDatabaseName(String)` - Sets the database name (defaults to `test`);
sets docker environment variable `FIREBIRD_DATABASE`

Firebird specific options are:

- `withEnableLegacyClientAuth()` - (_Firebird 3+_) Enables `LegacyAuth` and uses
it as the default for creating users, also relaxes `WireCrypt` to `Enabled`;
sets docker environment variable `EnableLegacyClientAuth` to `true`;
passes connection property `authPlugins` with value `Srp256,Srp,Legacy_Auth` if
this property is not explicitly set through `withUrlParam`
- `withEnableWireCrypt` - (_Firebird 3+_) Relaxes `WireCrypt` from `Required` to
`Enabled`; sets docker environment variable `EnableWireCrypt` to `true`
- `withTimeZone(String)` - Sets the time zone (defaults to JVM default time
zone); sets docker environment variable `TZ` to the specified value
- `withSysdbaPassword(String)` - Sets the SYSDBA password, but if 
`withUsername(String)` is set to `sysdba` (case-insensitive), this property is
ignored and the value of `withPassword` is used instead; sets docker
environment variable `ISC_PASSWORD` to the specified value

Example of use:

```java
import org.firebirdsql.testcontainers.FirebirdContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Simple test demonstrating use of {@code @Rule}.
 */
public class ExampleRuleTest {

  private static final DockerImageName IMAGE = 
          DockerImageName.parse(FirebirdContainer.IMAGE).withTag("3.0.7");

  @Rule
  public final FirebirdContainer<?> container = new FirebirdContainer<?>(IMAGE)
          .withUsername("testuser")
          .withPassword("testpassword");

  @Test
  public void canConnectToContainer() throws Exception {
    try (Connection connection = DriverManager
            .getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());
         Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("select CURRENT_USER from RDB$DATABASE")) {
      assertTrue("has row", rs.next());
      assertEquals("user name", "TESTUSER", rs.getString(1));
    }
  }
}
```

### Testcontainers URL

The testcontainers URL defines the container and connects to it. As long as 
there are active connections, the container will stay up.

For Firebird the URL format is:

- `jdbc:tc:firebird[:<image-tag>]://hostname/<databasename>[?<property>=<value>[&<property>=<value>...]]`
- `jdbc:tc:firebirdsql[:<image-tag>]://hostname/<databasename>[?<property>=<value>[&<property>=<value>...]]`

Where:

- `<image-tag>` (_optional, but recommended_) is the tag of the docker image to
  use, otherwise the default is used (which might change between versions)
- `<databasename>` (_optional_) is the name of the database (defaults to `test`)
- `<property>` is a connection property (Jaybird properties **and** testcontainers properties are possible) \
  Of special note are the properties:
  - `user` (_optional_) specifies the username to create and connect (defaults to `test`)
  - `password` (_optional_) specifies the password for the user (defaults to `test`)
- `<value>` is the value of the property

Example of use:

```java
/**
 * Simple test demonstrating use of url to instantiate container.
 */
public class ExampleUrlTest {

    @Test
    public void canConnectUsingUrl() throws Exception {
        try (Connection connection = DriverManager
                .getConnection("jdbc:tc:firebird://hostname/databasename?user=someuser&password=somepwd");
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select CURRENT_USER from RDB$DATABASE")) {
            assertTrue("has row", rs.next());
            assertEquals("user name", "SOMEUSER", rs.getString(1));
        }
    }
}
```

License
-------

See [LICENSE](LICENSE)
