Version History
===============

1.6.0
-----
- The _project_ image (`firebirdsql/firebird`) is now the default. \
  Contrary to previously announced, the 5.0.3 tag is the default, not 5.0.1. \
  If you want to continue using the _jacobalberty_ image, you'll need to explicitly reference the image name (e.g. using `FirebirdContainer.JACOB_ALBERTY_IMAGE`).
- As a backwards compatibility measure, a subset of tags of the _jacobalberty_ image are recognized in `jdbc:tc:firebird[sql]:...` URLs and `FirebirdContainerProvider.newInstance(String)`. \
  Specifically, tags starting with `2.`, `v2`, `v3`, `v4` and `v5` will select the _jacobalberty_ image instead of the _project_ image.
- Support for the _jacobalberty_ image is considered deprecated, but there are currently no plans to remove it. \
  Switch to the _project_ image  (`firebirdsql/firebird`).
- Support for the _fdcastel_ image was removed as it is no longer available. \
  Switch to the _project_ image (`firebirdsql/firebird`).
- Updated org.testcontainers:jdbc to 1.21.3
- Updated various test-dependencies
- Updated Maven build plugins

1.5.1
-----
- Added support for [firebirdsql/firebird](https://hub.docker.com/r/firebirdsql/firebird). \
  This was originally the _fdcastel_ image. \
  The name is defined in `FirebirdContainer.PROJECT_IMAGE`. \
  In 1.5.x, these images are not accessible as a `jdbc:tc:firebird[sql]:...` URL, only through `FirebirdContainer`; this will change with version 1.6.0. \
  All existing configuration options are mapped in a backwards compatible way.
- Defined `JACOB_ALBERTY_IMAGE` constants in `FirebirdContainer`.
- Support for the _fdcastel_ image is considered deprecated, but there are currently no plans to remove it. \
  Switch to the _project_ image.
- In the 1.6.0 release, the 5.0.1 version of the _project_ image will become the default image. \
  Make sure you explicitly use this image name (and a version tag) if you want to stick to the _jacobalberty_ image with the next release.
- Updated various test-dependencies

1.5.0
-----
- Updated org.testcontainers:jdbc to 1.20.4
- Updated various test-dependencies
- Updated Maven build plugins
- Added support for [ghcr.io/fdcastel/firebird](https://github.com/fdcastel/firebird-docker) images. \
  The name is defined in `FirebirdContainer.FDCASTEL_IMAGE`. \
  These images are not accessible as a `jdbc:tc:firebird[sql]:...` URL, only through `FirebirdContainer`.\
  All existing configuration options are mapped in a backwards compatible way.

1.4.0
-----
- Updated org.testcontainers:jdbc to 1.19.3
- Updated various test-dependencies
- Updated Maven build plugins
- Add javadoc on (main) classes similar to testcontainers-java

1.3.0
-----
- Updated org.testcontainers:jdbc to 1.17.6
- Updated various test-dependencies
- Updated default image version to Firebird 4.0.2. \
  Make sure you specify versions explicitly if you need to stay on Firebird 3.0.

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
