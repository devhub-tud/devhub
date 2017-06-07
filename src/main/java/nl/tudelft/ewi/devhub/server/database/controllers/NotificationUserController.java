package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.notifications.NotificationsToUsers;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Created by Arjan on 7-6-2017.
 */
public class NotificationUserController extends Controller<NotificationsToUsers>{

    @Inject
    public NotificationUserController(EntityManager entityManager) {
        super(entityManager);
    }
}
