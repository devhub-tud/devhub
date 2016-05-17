package nl.tudelft.ewi.devhub.server.database.entities.rubrics;

import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery.Review;

/**
 * A {@code GradingStrategy} describes how to translate a {@link Delivery} into a
 * {@link Review#getState()} and {@link Review#getGrade()}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface GradingStrategy {

    double createGrade(Delivery delivery);

    Delivery.State createState(Delivery delivery);

}
