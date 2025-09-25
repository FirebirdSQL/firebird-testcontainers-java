firebird-testcontainers-java
============================
[![Java CI with Maven](https://github.com/FirebirdSQL/firebird-testcontainers-java/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/FirebirdSQL/firebird-testcontainers-java/actions/workflows/maven.yml?query=branch%3Amaster)
[![MavenCentral](https://maven-badges.sml.io/sonatype-central/org.firebirdsql/firebird-testcontainers-java/badge.svg)](https://maven-badges.sml.io/sonatype-central/org.firebirdsql/firebird-testcontainers-java/)

Firebird-testcontainers-java is a module for [Testcontainers](https://www.testcontainers.org/)
to provide lightweight, throwaway instances of Firebird for JUnit tests.

The default Docker image used is [firebirdsql/firebird](https://hub.docker.com/r/firebirdsql/firebird), and also supports 
 [jacobalberty/firebird](https://hub.docker.com/r/jacobalberty/firebird/).

If you want to use Firebird 2.5, use the 2.5.x-sc (SuperClassic) variant of 
the `jacobalberty/firebird` image, or 2.5.9-ss as earlier versions of the 2.5.x-ss 
(SuperServer) variant seem to be broken. However, recently, it seems that the
2.5.x-sc variants also no longer work reliably.

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
testImplementation "org.firebirdsql:firebird-testcontainers-java:1.6.0"
```

### Maven

```xml
<dependency>
    <groupId>org.firebirdsql</groupId>
    <artifactId>firebird-testcontainers-java</artifactId>
    <version>1.6.0</version>
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

- `withUsername(String)` - Sets the username to create (defaults to `test`); sets docker environment variable `FIREBIRD_USER`. \
  For `jacobalberty/firebird`, if the value is `sysdba`, `FIREBIRD_USER` is not set.
- `withPassword(String)` - Sets the password of the user (defaults to `test`); sets the docker environment variable `FIREBIRD_PASSWORD`. \
  For `jacobalberty/firebird`, if the username is `sysdba`, `ISC_PASSWORD` is set instead of `FIREBIRD_PASSWORD`. \
  For `firebirdsql/firebird`, if the username is `sysdba`, it also sets `FIREBIRD_ROOT_PASSWORD`.
- `withDatabaseName(String)` - Sets the database name (defaults to `test`); sets docker environment variable `FIREBIRD_DATABASE`

Firebird specific options are:

- `withEnableLegacyClientAuth()` - (_Firebird 3+_) Enables `LegacyAuth` and uses it as the default for creating users, also relaxes `WireCrypt` to `Enabled`;
sets docker environment variable `EnableLegacyClientAuth` (`jacobalberty/firebird`) or `FIREBIRD_USE_LEGACY_AUTH` (`firebirdsql/firebird`) to `true`;
passes connection property `authPlugins` with value `Srp256,Srp,Legacy_Auth` if this property is not explicitly set through `withUrlParam`.
- `withEnableWireCrypt` - (_Firebird 3+_) Relaxes `WireCrypt` from `Required` to `Enabled`; 
sets docker environment variable `EnableWireCrypt` (`jacobalberty/firebird`) to `true`, or `FIREBIRD_CONF_WireCrypt` (`firebirdsql/firebird`) to `Enabled`.
- `withTimeZone(String)` - Sets the time zone (defaults to JVM default time zone); 
- sets docker environment variable `TZ` to the specified value
- `withSysdbaPassword(String)` - Sets the SYSDBA password, but if `withUsername(String)` is set to `sysdba` (case-insensitive), this property is ignored and the value of `withPassword` is used instead; 
sets docker environment variable `ISC_PASSWORD` (`jacobalberty/firebird`) or `FIREBIRD_ROOT_PASSWORD` (`firebirdsql/firebird`) to the specified value.

Example of use:

```java
import org.firebirdsql.testcontainers.FirebirdContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Simple test demonstrating use of {@code @Rule}.
 */
public class ExampleRuleTest {

  private static final DockerImageName IMAGE = 
          DockerImageName.parse(FirebirdContainer.IMAGE).withTag("5.0.3");

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
- `<property>` is a connection property (Jaybird properties **and** testcontainers
  properties are possible) \
  Of special note are the properties:
  - `user` (_optional_) specifies the username to create and connect (defaults to `test`)
  - `password` (_optional_) specifies the password for the user (defaults to `test`)
- `<value>` is the value of the property

These URLs use the `firebirdsql/firebird` images, except for tags starting with
`2.`, `v2`, `v3`, `v4` or `v5`, which will select the `jacobalberty/firebird`
images for backwards compatibility.

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
