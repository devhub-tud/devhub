package nl.tudelft.ewi.devhub.server.database;

import java.net.URI;

/**
 * Describes how the page for the entity can be accessed through the REST-API.
 */
public interface Base {

	/**
	 * @return The URI at which the entity is accessible.
	 */
	URI getURI();

}
