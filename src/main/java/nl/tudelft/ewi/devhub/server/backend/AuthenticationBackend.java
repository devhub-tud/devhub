package nl.tudelft.ewi.devhub.server.backend;

import com.google.inject.ImplementedBy;

/**
 * 
 * @author Michael
 *
 */
@ImplementedBy(AuthenticationBackendImpl.class)
public interface AuthenticationBackend {
	
	/**
	 * Authenticate a user
	 * 
	 * @param netId
	 *            The supplied netId
	 * @param password
	 *            The supplied password
	 * @return true if the credentials are correct
	 */
	boolean authenticate(String netId, String password);

}
