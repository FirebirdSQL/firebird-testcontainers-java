package org.firebirdsql.testcontainers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;

/**
 * Testcontainers implementation for Firebird.
 * <p>
 * Supported image: {@code jacobalberty/firebird}, {@code firebirdsql/firebird}, {@code ghcr.io/fdcastel/firebird}.
 * <p>
 * Exposed ports: 3050
 */
public class FirebirdContainer<SELF extends FirebirdContainer<SELF>> extends JdbcDatabaseContainer<SELF> {

    private static final Logger log = LoggerFactory.getLogger(FirebirdContainer.class);

    public static final String NAME = "firebird";
    public static final String ALTERNATE_NAME = "firebirdsql";
    public static final String PROJECT_IMAGE = "firebirdsql/firebird";
    public static final String JACOB_ALBERTY_IMAGE = "jacobalberty/firebird";
    /**
     * @deprecated Use {@link #PROJECT_IMAGE}
     */
    @Deprecated
    public static final String FDCASTEL_IMAGE = "ghcr.io/fdcastel/firebird";
    public static final String IMAGE = JACOB_ALBERTY_IMAGE;
    static final DockerImageName PROJECT_IMAGE_NAME = DockerImageName.parse(PROJECT_IMAGE);
    static final DockerImageName JACOB_ALBERTY_IMAGE_NAME = DockerImageName.parse(JACOB_ALBERTY_IMAGE);
    /**
     * @deprecated Use {@link #PROJECT_IMAGE_NAME}
     */
    @Deprecated
    static final DockerImageName FDCASTEL_IMAGE_NAME = DockerImageName.parse(FDCASTEL_IMAGE);
    static final DockerImageName DEFAULT_IMAGE_NAME = JACOB_ALBERTY_IMAGE_NAME;
    public static final String DEFAULT_TAG = "v4.0.2";

    public static final Integer FIREBIRD_PORT = 3050;
    private static final String FIREBIRD_SYSDBA = "sysdba";
    private static final int ARC4_REQUIRED_BITS = 160;
    private static final String CONNECTION_PROPERTY_AUTH_PLUGINS = "authPlugins";

    private String databaseName = "test";
    private String username = "test";
    private String password = "test";
    private boolean enableLegacyClientAuth;
    private String timeZone = ZoneId.systemDefault().getId();
    private boolean enableWireCrypt;
    private String sysdbaPassword;

    /**
     * Creates a Firebird container with the default image ({@link #IMAGE} and {@link #DEFAULT_TAG}).
     *
     * @deprecated Use explicit image using {@link #FirebirdContainer(DockerImageName)} or {@link #FirebirdContainer(String)}
     */
    @Deprecated
    public FirebirdContainer() {
        this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    /**
     * Creates a Firebird container with an image name (e.g. {@code "jacobalberty/firebird:3.0.7"}.
     *
     * @param dockerImageName Image name
     */
    public FirebirdContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    /**
     * Creates a Firebird container with a parsed image name.
     *
     * @param dockerImageName Parse image name
     */
    public FirebirdContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(PROJECT_IMAGE_NAME, JACOB_ALBERTY_IMAGE_NAME, FDCASTEL_IMAGE_NAME);

        addExposedPort(FIREBIRD_PORT);
    }

    @Override
    protected void configure() {
        ImageVariant variant = ImageVariant.of(getDockerImageName());
        variant.setTimeZone(this);
        variant.setDatabaseName(this);

        variant.setUserAndPassword(this);

        if (enableLegacyClientAuth) {
            variant.enableLegacyAuth(this);
            if (!urlParameters.containsKey(CONNECTION_PROPERTY_AUTH_PLUGINS)) {
                // Allow legacy auth with Jaybird 4, while also allowing Srp256 and Srp
                withUrlParam(CONNECTION_PROPERTY_AUTH_PLUGINS, "Srp256,Srp,Legacy_Auth");
            }
        }

        if (enableWireCrypt) {
            variant.setWireCryptEnabled(this);
        } else if (!isWireEncryptionSupported()) {
            log.warn("Java Virtual Machine does not support wire protocol encryption requirements. " +
                "Downgrading to EnableWireCrypt = true. To fix this, configure the JVM with unlimited strength Cryptographic Jurisdiction Policy.");
            variant.setWireCryptEnabled(this);
        }
    }

    @Override
    public String getDriverClassName() {
        return "org.firebirdsql.jdbc.FBDriver";
    }

    @Override
    public String getJdbcUrl() {
        String additionalUrlParams = constructUrlParameters("?", "&");
        return "jdbc:firebirdsql://" + getHost() + ":" + getMappedPort(FIREBIRD_PORT)
                + "/" + getDatabaseName() + additionalUrlParams;
    }

    @Override
    public String getDatabaseName() {
        if (isRunning()) {
            ImageVariant imageVariant = ImageVariant.of(getDockerImageName());
            switch (imageVariant) {
            case JACOBALBERTY:
            if (isFirebird25Image()) {
                // The 2.5 images of jacobalberty/firebird require an absolute path to access the database
                // Provide this value only when the container is running
                String databasePath = getEnvMap().getOrDefault("DBPATH", "/firebird/data");
                return databasePath + "/" + databaseName;
            }
            return databaseName;
            case PROJECT:
            case FDCASTEL:
                // The fdcastel/firebird images require an absolute path to access the database
                // Provide this value only when the container is running
                if (databaseName.charAt(0) != '/') {
                    return "/var/lib/firebird/data/" + databaseName;
                }
                return databaseName;
            }
        }
        return databaseName;
    }

    private boolean isFirebird25Image() {
        DockerImageName imageName = DockerImageName.parse(getDockerImageName());
        return imageName.getUnversionedPart().equals(IMAGE) && imageName.getVersionPart().startsWith("2.5");
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    protected String getTestQueryString() {
        return "select 1 from RDB$DATABASE";
    }

    @Override
    public SELF withDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
        return self();
    }

    @Override
    public SELF withUsername(final String username) {
        this.username = username;
        return self();
    }

    @Override
    public SELF withPassword(final String password) {
        this.password = password;
        return self();
    }

    /**
     * Enables legacy authentication plugin ({@code legacy_auth}) and use it as the default.
     *
     * @return this container
     */
    public SELF withEnableLegacyClientAuth() {
        this.enableLegacyClientAuth = true;
        return self();
    }

    /**
     * Relax wireCrypt setting from Required to Enabled.
     *
     * @return this container
     */
    public SELF withEnableWireCrypt() {
        this.enableWireCrypt = true;
        return self();
    }

    /**
     * Set the time zone of the image, defaults to the JVM default zone.
     *
     * @param timeZone Time zone name (prefer long names like Europe/Amsterdam)
     * @return this container
     */
    public SELF withTimeZone(final String timeZone) {
        this.timeZone = timeZone;
        return self();
    }

    /**
     * Set the sysdba password.
     * <p>
     * If {@code username} is {@code "sysdba"} (case insensitive), then {@code password} is used instead.
     * </p>
     *
     * @param sysdbaPassword Sysdba password
     * @return this container
     */
    public SELF withSysdbaPassword(final String sysdbaPassword) {
        this.sysdbaPassword = sysdbaPassword;
        return self();
    }

    @Override
    protected void waitUntilContainerStarted() {
        getWaitStrategy().waitUntilReady(this);
        super.waitUntilContainerStarted();
    }

    /**
     * Checks if the JVM meets the Jaybird (Firebird JDBC driver) requirements for encrypted connections.
     * <p>
     * Specifically, this checks if the ARC4 cipher can be used with 160 bit keys.
     * </p>
     *
     * @return {@code true} if Jaybird will be able to support encrypted connections on this JVM.
     */
    public static boolean isWireEncryptionSupported() {
        try {
            return Cipher.getMaxAllowedKeyLength("ARC4") >= ARC4_REQUIRED_BITS;
        } catch (NoSuchAlgorithmException e) {
            log.error("Cipher not found, JVM doesn't support encryption requirements", e);
            return false;
        }
    }

    private enum ImageVariant {
        PROJECT {
            @Override
            void setUserAndPassword(FirebirdContainer<?> container) {
                container.addEnv("FIREBIRD_USER", container.username);
                container.addEnv("FIREBIRD_PASSWORD", container.password);
                if (FIREBIRD_SYSDBA.equalsIgnoreCase(container.username)) {
                    container.addEnv("FIREBIRD_ROOT_PASSWORD", container.password);
                } else if (container.sysdbaPassword != null) {
                    container.addEnv("FIREBIRD_ROOT_PASSWORD", container.sysdbaPassword);
                }
            }

            @Override
            void enableLegacyAuth(FirebirdContainer<?> container) {
                container.addEnv("FIREBIRD_USE_LEGACY_AUTH", "true");
            }

            @Override
            void setWireCryptEnabled(FirebirdContainer<?> container) {
                container.addEnv("FIREBIRD_CONF_WireCrypt", "Enabled");
            }
        },
        JACOBALBERTY {
            @Override
            void setUserAndPassword(FirebirdContainer<?> container) {
                if (FIREBIRD_SYSDBA.equalsIgnoreCase(container.username)) {
                    container.addEnv("ISC_PASSWORD", container.password);
                } else {
                    container.addEnv("FIREBIRD_USER", container.username);
                    container.addEnv("FIREBIRD_PASSWORD", container.password);
                    if (container.sysdbaPassword != null) {
                        container.addEnv("ISC_PASSWORD", container.sysdbaPassword);
                    }
                }
            }

            @Override
            void enableLegacyAuth(FirebirdContainer<?> container) {
                container.addEnv("EnableLegacyClientAuth", "true");
            }

            @Override
            void setWireCryptEnabled(FirebirdContainer<?> container) {
                container.addEnv("EnableWireCrypt", "true");
            }
        },
        FDCASTEL {
            @Override
            void setUserAndPassword(FirebirdContainer<?> container) {
                PROJECT.setUserAndPassword(container);
            }

            @Override
            void enableLegacyAuth(FirebirdContainer<?> container) {
                PROJECT.enableLegacyAuth(container);
            }

            @Override
            void setWireCryptEnabled(FirebirdContainer<?> container) {
                PROJECT.setWireCryptEnabled(container);
            }
        };

        void setTimeZone(FirebirdContainer<?> container) {
            container.addEnv("TZ", container.timeZone);
        }

        void setDatabaseName(FirebirdContainer<?> container) {
            container.addEnv("FIREBIRD_DATABASE", container.databaseName);
        }

        abstract void setUserAndPassword(FirebirdContainer<?> container);

        abstract void enableLegacyAuth(FirebirdContainer<?> container);

        abstract void setWireCryptEnabled(FirebirdContainer<?> container);

        static ImageVariant of(String imageNameString) {
            DockerImageName imageName = DockerImageName.parse(imageNameString);
            if (imageName.isCompatibleWith(PROJECT_IMAGE_NAME)) {
                return PROJECT;
            } else if (imageName.isCompatibleWith(JACOB_ALBERTY_IMAGE_NAME)) {
                return JACOBALBERTY;
            } else if (imageName.isCompatibleWith(FDCASTEL_IMAGE_NAME)) {
                return FDCASTEL;
            }
            // Assume the default
            return JACOBALBERTY;
        }

    }
}
