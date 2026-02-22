package org.firebirdsql.testcontainers.examples;

import org.firebirdsql.testcontainers.FirebirdContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test demonstrating use of {@code @Testcontainers} and {@code @Container}.
 */
@Testcontainers
public class ExampleContainerTest {

    @Container
    public final FirebirdContainer container = new FirebirdContainer("firebirdsql/firebird:5.0.3")
            .withUsername("testuser")
            .withPassword("testpassword");

    @Test
    public void canConnectToContainer() throws Exception {
        try (Connection connection = DriverManager
                .getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select CURRENT_USER from RDB$DATABASE")) {
            assertTrue(rs.next(), "has row");
            assertEquals("TESTUSER", rs.getString(1), "user name");
        }
    }
}
