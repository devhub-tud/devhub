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
        return getCountFor(predicate) * 100 / amountOfGroups();
    }

    /**
     * @return the count of groups with a non-reviewed submission
     */
    public int getSubmittedCount() {
        return getCountFor(Delivery::isSubmitted);
    }

    /**
     * @return the count of groups with a rejected submission
     */
    public int getRejectedCount() {
        return getCountFor(Delivery::isRejected);
    }

    /**
     * @return the count of groups with a disapproved submission
     */
    public int getDisapprovedCount() {
        return getCountFor(Delivery::isDisapproved);
    }

    /**
     * @return the count of groups with a approved submission
     */
    public int getApprovedCount() {
        return getCountFor(Delivery::isApproved);
    }

    /**
     * @return the count of groups with a non-reviewed submission
     */
    public int getSubmittedPercentage() {
        return getPercentageFor(Delivery::isSubmitted);
    }

    /**
     * @return the percentage of groups with a rejected submission
     */
    public int getRejectedPercentage() {
        return getPercentageFor(Delivery::isRejected);
    }

    /**
     * @return the percentage of groups with a disapproved submission 
     */
    public int getDisapprovedPercentage() {
        return getPercentageFor(Delivery::isDisapproved);
    }

    /**
     * @return the percentage of groups with an approved percentage
     */
    public int getApprovedPercentage() {
        return getPercentageFor(Delivery::isApproved); 
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
