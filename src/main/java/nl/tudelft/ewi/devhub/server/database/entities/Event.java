package nl.tudelft.ewi.devhub.server.database.entities;

import java.util.Date;

/**
 * An action that was performed at a given time.
 */
public interface Event extends Comparable<Event> {

	/**
	 * @return Get the timestamp for this {@code Event}.
	 */
	Date getTimestamp();

	@Override
	default int compareTo(Event o) {
		return getTimestamp().compareTo(o.getTimestamp());
	}
	
}
