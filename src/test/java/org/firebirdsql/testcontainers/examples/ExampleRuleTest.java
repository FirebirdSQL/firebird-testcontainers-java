package org.firebirdsql.testcontainers.examples;

import org.firebirdsql.testcontainers.FirebirdContainer;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.firebirdsql.testcontainers.FirebirdTestImages.FIREBIRD_TEST_IMAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Simple test demonstrating use of {@code @Rule}.
 */
public class ExampleRuleTest {

    @Rule
    public final FirebirdContainer<?> container = new FirebirdContainer<>(FIREBIRD_TEST_IMAGE)
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
