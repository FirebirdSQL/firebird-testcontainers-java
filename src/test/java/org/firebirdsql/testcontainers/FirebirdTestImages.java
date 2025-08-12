package org.firebirdsql.testcontainers;

import org.testcontainers.utility.DockerImageName;

public final class FirebirdTestImages {

    public static final DockerImageName JACOB_ALBERTY_402_IMAGE = FirebirdContainer.JACOB_ALBERTY_IMAGE_NAME.withTag("v4.0.2");
    public static final DockerImageName JACOB_ALBERTY_259_SC_IMAGE = FirebirdContainer.JACOB_ALBERTY_IMAGE_NAME.withTag("2.5.9-sc");
    public static final DockerImageName JACOB_ALBERTY_259_SS_IMAGE = FirebirdContainer.JACOB_ALBERTY_IMAGE_NAME.withTag("2.5.9-ss");
    public static final DockerImageName PROJECT_503_IMAGE = FirebirdContainer.PROJECT_IMAGE_NAME.withTag("5.0.3");
    public static final DockerImageName PROJECT_TEST_IMAGE = PROJECT_503_IMAGE;
    public static final DockerImageName JACOB_ALBERTY_TEST_IMAGE = JACOB_ALBERTY_402_IMAGE;
    public static final DockerImageName FIREBIRD_TEST_IMAGE = PROJECT_TEST_IMAGE;

    private FirebirdTestImages() {
        throw new AssertionError("no instances");
    }
}
