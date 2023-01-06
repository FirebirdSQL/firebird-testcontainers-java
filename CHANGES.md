Version History
===============

1.3.0
-----
- Updated org.testcontainers:jdbc to 1.17.6
- Updated various test-dependencies

1.2.0
-----
- Updated org.testcontainers:jdbc to 1.16.2
- Updated junit:junit to 4.13.2
- Updated default image version to Firebird 3.0.8. \
  The next release will update the default image version to Firebird 4.0, so make sure you specify versions explicitly if you need to stay on Firebird 3.0.

1.1.0
-----
- Updated org.testcontainers:jdbc to 1.15.1
- Deprecated no-arg constructor of `FirebirdContainer` (see also <https://github.com/testcontainers/testcontainers-java/pull/2839>) \
  It is recommended to switch to using an explicit image name and version
- Added constructor `FirebirdContainer(DockerImageName)`. \
  Use with `DockerImageName.parse(FirebirdContainer.IMAGE).withTag("3.0.7")` to get an explicit version.
- Updated default image version to Firebird 3.0.7.

1.0.4
-----

- Update org.testcontainers:jdbc to 1.14.3
- Move static config in modules to constructor (see also <https://github.com/testcontainers/testcontainers-java/pull/2473>)
- Add `ContainerState#getHost` as a replacement for `getContainerIpAddress` (see also <https://github.com/testcontainers/testcontainers-java/pull/2742>)
- Added additional url params in `JdbcDatabaseContainer` (see also <https://github.com/testcontainers/testcontainers-java/issues/1802>)
- For compatibility with Jaybird 4, when legacy client auth is enabled and `authPlugins` URL param has not been explicitly added, add URL param `authPlugins` with value `Srp256,Srp,Legacy_auth`
- Updated default image version to Firebird 3.0.5.

  Because of intermittent connection problems in Firebird 3.0.6 (CORE-6346, CORE-6347, CORE-6348), 3.0.6 is not used as the default. Firebird 3.0.7 will fix this issue. 

1.0.3
-----

- Update org.testcontainers:jdbc to 1.12.5

1.0.2
-----

- Update org.testcontainers:jdbc to 1.12.0

1.0.1
-----

- Update org.testcontainers:jdbc to 1.11.4

1.0.0
-----

Initial version using org.testcontainers:jdbc 1.11.3
