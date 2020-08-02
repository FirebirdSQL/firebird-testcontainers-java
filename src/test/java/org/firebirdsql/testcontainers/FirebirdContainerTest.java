package org.firebirdsql.testcontainers;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.junit.Test;

import java.sql.*;

import static org.firebirdsql.testcontainers.FirebirdContainer.FIREBIRD_PORT;
import static org.firebirdsql.testcontainers.FirebirdContainer.IMAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.rnorth.visibleassertions.VisibleAssertions.*;

public class FirebirdContainerTest {

    @Test
    public void testWithSysdbaPassword() throws SQLException {
        final String sysdbaPassword = "sysdbapassword";
        try (FirebirdContainer<?> container = new FirebirdContainer<>().withSysdbaPassword(sysdbaPassword)) {
            container.start();

            try (Connection connection = DriverManager.getConnection(container.getJdbcUrl(), "sysdba", sysdbaPassword)) {
                assertTrue("Connection is valid", connection.isValid(100));
            }
        }
    }

    /**
     * With {@code username} set to sysdba, {@code password} should take precedence over {@code sysdbaPassword}
     */
    @Test
    public void testUserPasswordTakesPrecedenceOverWithSysdbaPassword() throws SQLException {
        final String userPassword = "password1";
        final String withSysdbaPassword = "password2";
        try (FirebirdContainer<?> container = new FirebirdContainer<>().withUsername("sysdba").withPassword(userPassword).withSysdbaPassword(withSysdbaPassword)) {
            container.start();

            try (Connection connection = DriverManager.getConnection(container.getJdbcUrl(), "sysdba", userPassword)) {
                assertTrue("Connection is valid", connection.isValid(100));
            }
        }
    }

    @Test
    public void testWithEnableLegacyClientAuth() throws SQLException {
        try (FirebirdContainer<?> container = new FirebirdContainer<>().withEnableLegacyClientAuth()) {
            container.start();

            try (Connection connection = container.createConnection("");
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("select MON$AUTH_METHOD from MON$ATTACHMENTS where MON$ATTACHMENT_ID = CURRENT_CONNECTION")) {
                assertTrue("Expected a row", rs.next());
                assertEquals("Authentication method should be Legacy_Auth", "Legacy_Auth", rs.getString("MON$AUTH_METHOD"));
            }
        }
    }

    @Test
    public void testWithEnableWireCrypt() throws SQLException {
        try (FirebirdContainer<?> container = new FirebirdContainer<>().withEnableWireCrypt()) {
            container.start();

            if (FirebirdContainer.isWireEncryptionSupported()) {
                // Check connecting with wire crypt
                try (Connection connection = container.createConnection("")) {
                    GDSServerVersion serverVersion = connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
                    assertTrue("Expected encryption in use", serverVersion.isWireEncryptionUsed());
                }
            }

            try (Connection connection = container.createConnection("?wireCrypt=disabled")) {
                GDSServerVersion serverVersion = connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
                assertFalse("Expected encryption not in use", serverVersion.isWireEncryptionUsed());
            }
        }
    }

    /**
     * The 2.5 images of jacobalberty/firebird handle FIREBIRD_DATABASE and need an absolute path to access the database
     */
    @Test
    public void test258_scImage() throws Exception {
        try (FirebirdContainer<?> container = new FirebirdContainer<>(IMAGE + ":2.5.8-sc").withDatabaseName("test")) {
            assertEquals("Expect original database name before start",
                    "test", container.getDatabaseName());

            container.start();

            assertEquals("Expect modified database name after start",
                    "/firebird/data/test", container.getDatabaseName());

            try (Connection connection = DriverManager
                    .getConnection("jdbc:firebirdsql://" + container.getContainerIpAddress() + ":" + container.getMappedPort(FIREBIRD_PORT) + "/" + container.getDatabaseName(),
                            container.getUsername(), container.getPassword())
            ) {
                assertTrue(connection.isValid(1000));
            }
        }
    }
}
