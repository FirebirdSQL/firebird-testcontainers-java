package org.firebirdsql.testcontainers;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.firebirdsql.testcontainers.FirebirdContainer.FIREBIRD_PORT;
import static org.firebirdsql.testcontainers.FirebirdTestImages.FIREBIRD_259_SC_IMAGE;
import static org.firebirdsql.testcontainers.FirebirdTestImages.FIREBIRD_259_SS_IMAGE;
import static org.firebirdsql.testcontainers.FirebirdTestImages.FIREBIRD_TEST_IMAGE;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdContainerTest {

    @Test
    void testWithSysdbaPassword() throws SQLException {
        final String sysdbaPassword = "sysdbapassword";
        try (FirebirdContainer<?> container = new FirebirdContainer<>(FIREBIRD_TEST_IMAGE)
                .withSysdbaPassword(sysdbaPassword)) {
            container.start();

            try (Connection connection = DriverManager.getConnection(container.getJdbcUrl(), "sysdba", sysdbaPassword)) {
                assertTrue(connection.isValid(100), "Connection is valid");
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    void testImplicitImage() throws SQLException {
        final String sysdbaPassword = "sysdbapassword";
        try (FirebirdContainer<?> container = new FirebirdContainer<>()
                .withSysdbaPassword(sysdbaPassword)) {
            container.start();

            try (Connection connection = DriverManager.getConnection(container.getJdbcUrl(), "sysdba", sysdbaPassword)) {
                assertTrue(connection.isValid(100), "Connection is valid");
            }
        }
    }

    /**
     * With {@code username} set to sysdba, {@code password} should take precedence over {@code sysdbaPassword}
     */
    @Test
    void testUserPasswordTakesPrecedenceOverWithSysdbaPassword() throws SQLException {
        final String userPassword = "password1";
        final String withSysdbaPassword = "password2";
        try (FirebirdContainer<?> container = new FirebirdContainer<>(FIREBIRD_TEST_IMAGE)
                .withUsername("sysdba").withPassword(userPassword).withSysdbaPassword(withSysdbaPassword)) {
            container.start();

            try (Connection connection = DriverManager.getConnection(container.getJdbcUrl(), "sysdba", userPassword)) {
                assertTrue(connection.isValid(100), "Connection is valid");
            }
        }
    }

    @Test
    void testWithEnableLegacyClientAuth() throws SQLException {
        try (FirebirdContainer<?> container = new FirebirdContainer<>(FIREBIRD_TEST_IMAGE)
                .withEnableLegacyClientAuth()) {
            container.start();

            try (Connection connection = container.createConnection("");
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("select MON$AUTH_METHOD from MON$ATTACHMENTS where MON$ATTACHMENT_ID = CURRENT_CONNECTION")) {
                assertTrue(rs.next(), "Expected a row");
                assertEquals("Legacy_Auth", rs.getString("MON$AUTH_METHOD"), "Authentication method should be Legacy_Auth");
            }
        }
    }

    @Test
    void testWithEnableLegacyClientAuth_jdbcUrlIncludeAuthPlugins_default() {
        try (FirebirdContainer<?> container = new FirebirdContainer<>(FIREBIRD_TEST_IMAGE)
                .withEnableLegacyClientAuth()) {
            container.start();

            String jdbcUrl = container.getJdbcUrl();
            assertThat(jdbcUrl, allOf(
                    containsString("?"),
                    containsString("authPlugins=Srp256,Srp,Legacy_Auth")));
        }
    }

    @Test
    void testWithEnableLegacyClientAuth_jdbcUrlIncludeAuthPlugins_explicitlySet() {
        try (FirebirdContainer<?> container = new FirebirdContainer<>(FIREBIRD_TEST_IMAGE)
                .withEnableLegacyClientAuth()
                .withUrlParam("authPlugins", "Legacy_Auth")) {
            container.start();

            String jdbcUrl = container.getJdbcUrl();
            assertThat(jdbcUrl, allOf(
                    containsString("?"),
                    containsString("authPlugins=Legacy_Auth")));
        }
    }

    @Test
    void testWithEnableWireCrypt() throws SQLException {
        try (FirebirdContainer<?> container = new FirebirdContainer<>(FIREBIRD_TEST_IMAGE).withEnableWireCrypt()) {
            container.start();

            if (FirebirdContainer.isWireEncryptionSupported()) {
                // Check connecting with wire crypt
                try (Connection connection = container.createConnection("")) {
                    GDSServerVersion serverVersion = connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
                    assertTrue(serverVersion.isWireEncryptionUsed(), "Expected encryption in use");
                }
            }

            try (Connection connection = container.createConnection("?wireCrypt=disabled")) {
                GDSServerVersion serverVersion = connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
                assertFalse(serverVersion.isWireEncryptionUsed(), "Expected encryption not in use");
            }
        }
    }

    /**
     * The 2.5 images of jacobalberty/firebird handle FIREBIRD_DATABASE and need an absolute path to access the database
     */
    @Test
    void test259_scImage() throws Exception {
        try (FirebirdContainer<?> container = new FirebirdContainer<>(FIREBIRD_259_SC_IMAGE).withDatabaseName("test")) {
            assertEquals("test", container.getDatabaseName(), "Expect original database name before start");

            container.start();

            assertEquals("/firebird/data/test", container.getDatabaseName(),
                    "Expect modified database name after start");

            try (Connection connection = DriverManager
                    .getConnection("jdbc:firebirdsql://" + container.getHost() + ":" + container.getMappedPort(FIREBIRD_PORT) + "/" + container.getDatabaseName(),
                            container.getUsername(), container.getPassword())
            ) {
                assertTrue(connection.isValid(1000));
            }
        }
    }

    /**
     * The 2.5 images of jacobalberty/firebird handle FIREBIRD_DATABASE and need an absolute path to access the database
     * <p>
     * NOTE: This test is ignored because it occasionally fails locally and repeatedly on GitHub Actions.
     * </p>
     */
    @Disabled
    @Test
    void test259_ssImage() throws Exception {
        try (FirebirdContainer<?> container = new FirebirdContainer<>(FIREBIRD_259_SS_IMAGE).withDatabaseName("test")) {
            assertEquals("test", container.getDatabaseName(), "Expect original database name before start");

            container.start();

            assertEquals("/firebird/data/test", container.getDatabaseName(),
                    "Expect modified database name after start");

            try (Connection connection = DriverManager
                    .getConnection("jdbc:firebirdsql://" + container.getHost() + ":" + container.getMappedPort(FIREBIRD_PORT) + "/" + container.getDatabaseName(),
                            container.getUsername(), container.getPassword())
            ) {
                assertTrue(connection.isValid(1000));
            }
        }
    }

    @Test
    void testWithAdditionalUrlParamInJdbcUrl() {
        try (FirebirdContainer<?> firebird = new FirebirdContainer<>(FIREBIRD_TEST_IMAGE)
                .withUrlParam("charSet", "utf-8")
                .withUrlParam("blobBufferSize", "2048")) {

            firebird.start();
            String jdbcUrl = firebird.getJdbcUrl();
            assertThat(jdbcUrl, allOf(
                    containsString("?"),
                    containsString("&"),
                    containsString("blobBufferSize=2048"),
                    containsString("charSet=utf-8")));
        }
    }
}
