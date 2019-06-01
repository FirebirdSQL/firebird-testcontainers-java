package org.firebirdsql.testcontainers.examples;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;

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
