package org.firebirdsql.testcontainers;

import org.testcontainers.utility.DockerImageName;

public final class FirebirdTestImages {

    public static final DockerImageName FIREBIRD_402_IMAGE = FirebirdContainer.DEFAULT_IMAGE_NAME.withTag("v4.0.2");
    public static final DockerImageName FIREBIRD_259_SC_IMAGE = FirebirdContainer.DEFAULT_IMAGE_NAME.withTag("2.5.9-sc");
    public static final DockerImageName FIREBIRD_259_SS_IMAGE = FirebirdContainer.DEFAULT_IMAGE_NAME.withTag("2.5.9-ss");
    public static final DockerImageName FDCASTEL_501_IMAGE = FirebirdContainer.FDCASTEL_IMAGE_NAME.withTag("5.0.1");
    public static final DockerImageName FIREBIRD_TEST_IMAGE = FIREBIRD_402_IMAGE;
    public static final DockerImageName FDCASTEL_TEST_IMAGE = FDCASTEL_501_IMAGE;

    private FirebirdTestImages() {
        throw new AssertionError("no instances");
    }
}
