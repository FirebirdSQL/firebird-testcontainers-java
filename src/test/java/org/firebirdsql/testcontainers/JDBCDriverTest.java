package org.firebirdsql.testcontainers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.testcontainers.jdbc.ContainerDatabaseDriver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;

import static java.util.Arrays.asList;
import static org.rnorth.visibleassertions.VisibleAssertions.*;

@RunWith(Parameterized.class)
public class JDBCDriverTest {

    private enum Options {
        ScriptedSchema,
        JDBCParams,
    }

    @Parameter
    public String jdbcUrl;
    @Parameter(1)
    public EnumSet<Options> options;

    @Parameterized.Parameters(name = "{index} - {0}")
    public static Iterable<Object[]> data() {
        return asList(
            new Object[][]{
                {"jdbc:tc:firebird://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", EnumSet.of(Options.ScriptedSchema, Options.JDBCParams)},
                {"jdbc:tc:firebird:v4.0.0://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", EnumSet.of(Options.ScriptedSchema, Options.JDBCParams)},
                {"jdbc:tc:firebird:v3.0.8://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", EnumSet.of(Options.ScriptedSchema, Options.JDBCParams)},
                {"jdbc:tc:firebird:3.0.7://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", EnumSet.of(Options.ScriptedSchema, Options.JDBCParams)},
                {"jdbc:tc:firebirdsql:v3.0.8://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", EnumSet.of(Options.ScriptedSchema, Options.JDBCParams)},
                {"jdbc:tc:firebirdsql:2.5.9-sc://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", EnumSet.of(Options.ScriptedSchema, Options.JDBCParams)},
                {"jdbc:tc:firebirdsql:2.5.9-ss://hostname/databasename?user=someuser&password=somepwd&charSet=utf-8&TC_INITFUNCTION=org.firebirdsql.testcontainers.JDBCDriverTest::sampleInitFunction", EnumSet.of(Options.ScriptedSchema, Options.JDBCParams)},
            });
    }

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

    @AfterClass
    public static void testCleanup() {
        ContainerDatabaseDriver.killContainers();
    }

    @Test
    public void test() throws SQLException {
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
            assertEquals("A basic SELECT query succeeds", 1, resultSetInt);
            return true;
        });

        assertTrue("The database returned a record as expected", result);
    }

    private void performTestForScriptedSchema(DataSource dataSource) throws SQLException {
        new QueryRunner(dataSource).query("SELECT foo FROM bar WHERE foo LIKE '%world'", rs -> {
            rs.next();
            String resultSetString = rs.getString(1);
            assertEquals("A basic SELECT query succeeds where the schema has been applied from a script", "hello world", resultSetString);
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
            assertEquals("User from query param is created.", "SOMEUSER", resultUser);
            return true;
        });

        assertTrue("The database returned a record as expected", result);

        String databaseQuery = "select rdb$get_context('SYSTEM', 'DB_NAME') from RDB$DATABASE";

        result = new QueryRunner(dataSource).query(databaseQuery, rs -> {
            rs.next();
            String resultDB = rs.getString(1);
            // Firebird reports full path
            assertThat("Database name from URL String is used.", resultDB, CoreMatchers.endsWith("/databasename"));
            return true;
        });

        assertTrue("The database returned a record as expected", result);
    }

    private HikariDataSource getDataSource(String jdbcUrl, int poolSize) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setConnectionTestQuery("SELECT 1 FROM RDB$DATABASE");
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(poolSize);

        return new HikariDataSource(hikariConfig);
    }

}
