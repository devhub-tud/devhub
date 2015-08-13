package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.entities.comments.PullRequestComment;

import javax.persistence.EntityManager;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class PullRequestComments extends Controller<PullRequestComment> {

    @Inject
    public PullRequestComments(EntityManager entityManager) {
        super(entityManager);
    }

}
