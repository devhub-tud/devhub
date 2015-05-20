package nl.tudelft.ewi.devhub.server.web.resources.views;

import lombok.Data;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LineWarning;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
public class WarningResolver {

    private final List<LineWarning> warnings;

    public List<LineWarning> retrieveWarnings(final String commitId,
                                              final String fileName,
                                              final Integer lineNumber) {
        return warnings.stream()
				.filter(warning -> warning.getSource().equals(commitId, fileName, lineNumber))
				.collect(Collectors.toList());
    }

}
