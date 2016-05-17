package nl.tudelft.ewi.devhub.server.database.entities.rubrics;

import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery.State;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class DutchGradingStrategy implements GradingStrategy {

    public static final double THRESHOLD = 5.75;

    @Override
    public double createGrade(Delivery delivery) {
        return Math.max(
            1,
            delivery.getAchievedNumberOfPoints() / delivery.getAssignment().getNumberOfAchievablePoints() * 10
        );
    }

    @Override
    public State createState(Delivery delivery) {
        return createGrade(delivery) >= THRESHOLD ? State.APPROVED : State.DISAPPROVED;
    }

}
