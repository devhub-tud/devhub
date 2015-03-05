package nl.tudelft.ewi.devhub.server.backend;

import lombok.Data;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;

import java.util.List;

/**
* Created by jgmeligmeyling on 05/03/15.
*/
@Data
public class AssignmentStats {

    private final List<Delivery> deliveries;
    private final List<Group> groups;

    private int getPercentageFor(Delivery.State state) {
        if(deliveries.isEmpty()) {
            return 0;
        }
        return (int) (deliveries.stream().filter((delivery) ->
                delivery.hasState(state))
                .count() * 100 / groups.size());
    }

    public int amountOfSubmissions() {
        return deliveries.size();
    }

    public int getSubmittedPercentage() {
        return getPercentageFor(Delivery.State.SUBMITTED);
    }

    public int getRejectedPercentage() {
        return getPercentageFor(Delivery.State.REJECTED);
    }

    public int getDisapprovedPercentage() {
        return getPercentageFor(Delivery.State.DISAPPROVED);
    }

    public int getApprovedPercentage() {
        return getPercentageFor(Delivery.State.APPROVED);
    }

}
