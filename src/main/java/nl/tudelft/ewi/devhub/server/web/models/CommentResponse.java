package nl.tudelft.ewi.devhub.server.web.models;

import lombok.Data;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
public class CommentResponse {

    private String name;

    private String date;

    private String content;

}
