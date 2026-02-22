package org.firebirdsql.testcontainers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.jdbc.ContainerDatabaseDriver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ParameterizedClass(name = "{index} - {0}")
@MethodSource("data")
public class JDBCDriverTest {

    // NOTE Class needs to be public due to sampleInitFunction referenced in test URLs

    private enum Options {
        ScriptedSchema,
        JDBCParams,
    }

    private final String jdbcUrl;
    private final Set<Options> options;

    JDBCDriverTest(String jdbcUrl, Set<Options> options) {
        this.jdbcUrl = jdbcUrl;
        this.options = options;
    }

    static Stream<Arguments> data() {
        return Stream.of(
                testCase("jdbc:tc:firebird://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                testCase("jdbc:tc:firebirdsql://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                testCase("jdbc:tc:firebird:latest://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                testCase("jdbc:tc:firebird:5://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                testCase("jdbc:tc:firebird:5.0.3://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                testCase("jdbc:tc:firebirdsql:5.0.3://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                testCase("jdbc:tc:firebird:4://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                testCase("jdbc:tc:firebird:4.0.6://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                testCase("jdbc:tc:firebird:3://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                testCase("jdbc:tc:firebird:3.0.13://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),

                // jacobalberty tags are mapped
                testCase("jdbc:tc:firebird:v4.0.2://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                testCase("jdbc:tc:firebird:v3.0.10://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams)
                // These images are problematic (they don't always seem to have their port available, or connecting doesn't work)
                //testCase("jdbc:tc:firebird:2.5.9-sc://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams),
                //testCase("jdbc:tc:firebird:2.5.9-ss://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", Options.ScriptedSchema, Options.JDBCParams)
        );
    }

    @SuppressWarnings("SameParameterValue")
    private static Arguments testCase(String jdbcUrl, Options... options) {
        Set<Options> optionSet = EnumSet.noneOf(Options.class);
        optionSet.addAll(asList(options));
        return Arguments.of(jdbcUrl, optionSet);
    }

    // Must be public: referenced in test URLs
    @SuppressWarnings("unused")
    public static void sampleInitFunction(Connection connection) throws SQLException {
        connection.createStatement().execute("CREATE TABLE bar (\n" +
            "  foo VARCHAR(255)\n" +
            ");");
        connection.createStatement().execute("INSERT INTO bar (foo) VALUES ('hello world');");
        connection.createStatement().execute("CREATE TABLE my_counter (\n" +
            "  n INT\n" +
            ");");
    }

    @AfterAll
    static void testCleanup() {
        ContainerDatabaseDriver.killContainers();
    }

    @Test
    void test() throws SQLException {
        try (HikariDataSource dataSource = getDataSource(jdbcUrl, 1)) {
            performSimpleTest(dataSource);

            if (options.contains(Options.ScriptedSchema)) {
                performTestForScriptedSchema(dataSource);
            }

            if (options.contains(Options.JDBCParams)) {
                performTestForJDBCParamUsage(dataSource);
            }
        }
    }

    private void performSimpleTest(DataSource dataSource) throws SQLException {
        boolean result = new QueryRunner(dataSource).query("SELECT 1 FROM RDB$DATABASE", rs -> {
            rs.next();
            int resultSetInt = rs.getInt(1);
            assertEquals(1, resultSetInt, "A basic SELECT query succeeds");
            return true;
        });

        assertTrue(result, "The database returned a record as expected");
    }

    private void performTestForScriptedSchema(DataSource dataSource) throws SQLException {
        new QueryRunner(dataSource).query("SELECT foo FROM bar WHERE foo LIKE '%world'", rs -> {
            rs.next();
            String resultSetString = rs.getString(1);
            assertEquals("hello world", resultSetString,
                    "A basic SELECT query succeeds where the schema has been applied from a script");
            return true;
        });
    }

    private void performTestForJDBCParamUsage(DataSource dataSource) throws SQLException {
        boolean result = new QueryRunner(dataSource).query("select CURRENT_USER FROM RDB$DATABASE", rs -> {
            rs.next();
            String resultUser = rs.getString(1);
            // Not all databases (eg. Postgres) return @% at the end of user name. We just need to make sure the user name matches.
            if (resultUser.endsWith("@%")) {
                resultUser = resultUser.substring(0, resultUser.length() - 2);
            }
            assertEquals("SOMEUSER", resultUser, "User from query param is created");
            return true;
        });

        assertTrue(result, "The database returned a record as expected");

        String databaseQuery = "select rdb$get_context('SYSTEM', 'DB_NAME') from RDB$DATABASE";

        result = new QueryRunner(dataSource).query(databaseQuery, rs -> {
            rs.next();
            String resultDB = rs.getString(1);
            // Firebird reports full path
            assertThat("Database name from URL String is used.", resultDB, endsWith("/databasename"));
            return true;
        });

        assertTrue(result, "The database returned a record as expected");
    }

    @SuppressWarnings("SameParameterValue")
    private HikariDataSource getDataSource(String jdbcUrl, int poolSize) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setConnectionTestQuery("SELECT 1 FROM RDB$DATABASE");
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(poolSize);

        return new HikariDataSource(hikariConfig);
    }

}
