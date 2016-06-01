package nl.tudelft.ewi.devhub.server.util;

import lombok.Data;

/**
 * Created by Douwe Koopmans on 1-6-16.
 */
@Data(staticConstructor = "of")
public class Version {
    private final String mavenVersion;
    private final String commitRef;
    private final String closestGitTag;
}
