package org.firebirdsql.testcontainers;

import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;

/**
 * Simple test demonstrating use of {@code @Rule}.
 */
public class ExampleTest {

    @Rule
    public final FirebirdContainer container = new FirebirdContainer();

    @Test
    public void canConnectToContainer() throws Exception {
        try (Connection connection = DriverManager
                .getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select CURRENT_USER from RDB$DATABASE")) {
            assertTrue("has row", rs.next());
            assertEquals("user name", container.getUsername().toUpperCase(), rs.getString(1));
        }
    }
}
