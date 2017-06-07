package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.entities.AssignedTA;

import javax.persistence.EntityManager;

/**
 * Created by sayra on 07/06/2017.
 */
public class AssignedTAs extends Controller<AssignedTA> {

    @Inject
    public AssignedTAs(EntityManager em) {
        super(em);
    }

}
