package nl.tudelft.ewi.devhub.server.backend;

import lombok.Data;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;

import java.util.List;
import java.util.function.Predicate;

/**
* @author Jan-Willem Gmelig Meyling
*/
@Data
public class AssignmentStats {

    private final List<Delivery> deliveries;
    private final List<Group> groups;
    
    private int getCountFor(Predicate<? super Delivery> predicate) {
        return (int) deliveries.stream().filter(predicate).count();
    }
    
    private int getPercentageFor(Predicate<? super Delivery> predicate) {
        int amountOfGroups = amountOfGroups();
        if(amountOfGroups == 0){
            return 0;
        }
        return getCountFor(predicate) * 100 / amountOfGroups;
    }

    /**
     * @param state
     * @return the count for the state
     */
    public int getCountFor(Delivery.State state) {
        return getCountFor(delivery -> delivery.getState().equals(state));
    }

    /**
     * @param state
     * @return the count for the state
     */
    public int getPercentageFor(Delivery.State state) {
        return getPercentageFor(delivery -> delivery.getState().equals(state));
    }

    /**
     * @return the total amount of groups
     */
    public int amountOfGroups() {
        return groups.size();
    }

    /**
     * @return the total amount of submissions
     */
    public int amountOfSubmissions() {
        return deliveries.size();
    }


}
