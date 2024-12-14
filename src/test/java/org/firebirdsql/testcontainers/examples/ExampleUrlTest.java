package org.firebirdsql.testcontainers.examples;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test demonstrating use of url to instantiate container.
 */
class ExampleUrlTest {

    @Test
    void canConnectUsingUrl() throws Exception {
        try (Connection connection = DriverManager
                .getConnection("jdbc:tc:firebird://hostname/databasename?user=someuser&password=somepwd");
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select CURRENT_USER from RDB$DATABASE")) {
            assertTrue(rs.next(), "has row");
            assertEquals("SOMEUSER", rs.getString(1), "user name");
        }
    }
}
