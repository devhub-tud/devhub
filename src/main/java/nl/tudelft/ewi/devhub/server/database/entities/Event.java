package nl.tudelft.ewi.devhub.server.database.entities;

import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * An action that was performed at a given time.
 */
@MappedSuperclass
public abstract class Event implements Comparable<Event> {

	/**
	 * @return Get the timestamp for this {@code Event}.
	 */
	public abstract Date getTimestamp();

	@Override
	public int compareTo(Event o) {
		return getTimestamp().compareTo(o.getTimestamp());
	}
}
