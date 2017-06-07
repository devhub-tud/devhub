package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.notifications.Notification;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Created by Arjan on 7-6-2017.
 */
public class NotificationController extends Controller<Notification>{

    @Inject
    public NotificationController(EntityManager entityManager) {
        super(entityManager);
    }
}
