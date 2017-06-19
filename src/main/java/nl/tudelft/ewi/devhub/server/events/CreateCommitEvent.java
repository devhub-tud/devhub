package nl.tudelft.ewi.devhub.server.events;

import lombok.Data;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data public class CreateCommitEvent {

    private String repositoryName;

    private String commitId;

}
