package nl.tudelft.ewi.devhub.server.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitPush {

    private String repository;

}
