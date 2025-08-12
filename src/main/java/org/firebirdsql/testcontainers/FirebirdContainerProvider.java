package org.firebirdsql.testcontainers;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.jdbc.ConnectionUrl;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

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

    // Two character prefix of tags of the jacobalberty image
    // The firebirdsql image uses versions without v prefix and doesn't provide a Firebird 2.x image
    private static final Set<String> LEGACY_TAG_PREFIXES = unmodifiableSet(new HashSet<>(
            Arrays.asList("2.", "v2", "v3", "v4", "v5")));

    private static boolean isLegacyTag(String tag) {
        return tag.length() >= 2 && LEGACY_TAG_PREFIXES.contains(tag.substring(0, 2));
    }

    @Override
    public JdbcDatabaseContainer newInstance(String tag) {
        DockerImageName imageName = isLegacyTag(tag)
                ? FirebirdContainer.JACOB_ALBERTY_IMAGE_NAME
                : FirebirdContainer.DEFAULT_IMAGE_NAME;
        return new FirebirdContainer(imageName.withTag(tag));
    }

    @Override
    public JdbcDatabaseContainer newInstance(ConnectionUrl connectionUrl) {
        return newInstanceFromConnectionUrl(connectionUrl, USER_PARAM, PASSWORD_PARAM);
    }
}
