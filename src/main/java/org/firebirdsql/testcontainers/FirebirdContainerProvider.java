package org.firebirdsql.testcontainers;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.jdbc.ConnectionUrl;
import org.testcontainers.utility.DockerImageName;

/**
 * Factory for Firebird containers.
 */
public class FirebirdContainerProvider extends JdbcDatabaseContainerProvider {

    public static final String USER_PARAM = "user";
    public static final String PASSWORD_PARAM = "password";

    @Override
    public boolean supports(String databaseType) {
        return FirebirdContainer.NAME.equals(databaseType) || FirebirdContainer.ALTERNATE_NAME.equals(databaseType);
    }

    @Override
    public JdbcDatabaseContainer newInstance() {
        return newInstance(FirebirdContainer.DEFAULT_TAG);
    }

    @Override
    public JdbcDatabaseContainer newInstance(String tag) {
        return new FirebirdContainer(DockerImageName.parse(FirebirdContainer.IMAGE).withTag(tag));
    }

    @Override
    public JdbcDatabaseContainer newInstance(ConnectionUrl connectionUrl) {
        return newInstanceFromConnectionUrl(connectionUrl, USER_PARAM, PASSWORD_PARAM);
    }
}
