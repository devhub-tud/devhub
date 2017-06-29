package nl.tudelft.ewi.devhub.server.database.entities.notifications;

import nl.tudelft.ewi.devhub.server.database.entities.User;

import java.util.Set;

/**
 * Created by jgmeligmeyling on 28/06/2017.
 */
public interface Watchable {

    Set<User> getWatchers();

}
