package nl.tudelft.ewi.devhub.server.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitPush {

    @NotEmpty private String repository;

}
