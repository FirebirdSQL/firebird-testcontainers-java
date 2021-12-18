package org.firebirdsql.testcontainers;

import org.testcontainers.utility.DockerImageName;

public final class FirebirdTestImages {

    public static final DockerImageName FIREBIRD_308_IMAGE = FirebirdContainer.DEFAULT_IMAGE_NAME.withTag("v3.0.8");
    public static final DockerImageName FIREBIRD_259_SC_IMAGE = FirebirdContainer.DEFAULT_IMAGE_NAME.withTag("2.5.9-sc");
    public static final DockerImageName FIREBIRD_259_SS_IMAGE = FirebirdContainer.DEFAULT_IMAGE_NAME.withTag("2.5.9-ss");
    public static final DockerImageName FIREBIRD_TEST_IMAGE = FIREBIRD_308_IMAGE;

    private FirebirdTestImages() {
        throw new AssertionError("no instances");
    }
}
