package nl.tudelft.ewi.devhub.server.backend;

import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.inject.ImplementedBy;

import java.io.IOException;

/**
 * An {@code AuthenticationProvider} should be implemented to authenticate users
 * and synchronize their profile to the users directory.
 * 
 * @author Jan-Willem
 *
 */
public interface AuthenticationProvider {
	
	/**
	 * Authenticate a user.
	 * 
	 * @param username
	 *            The supplied username
	 * @param password
	 *            The supplied password
	 * @return A {@link AuthenticationSession}, to allow the
	 *         {@code AuthenticationProvider} to synchronize user credentials
	 *         within the same directory connection
	 * @throws AuthenticationProviderUnavailable
	 *             If the {@code AuthenticationProvider} is unavailable or an
	 *             unexpected error occurred.
	 * @throws InvalidCredentialsException
	 *             If the supplied credentials are invalid
	 */
	AuthenticationSession authenticate(String username, String password)
			throws AuthenticationProviderUnavailable, InvalidCredentialsException;
	
	/**
	 * Allow the {@link AuthenticationProvider} for some further directory
	 * access. This interface extends the autoclosable interface because the
	 * directory connection probably needs to be disconnected.
	 * 
	 * @author Jan-Willem
	 *
	 */
	@ImplementedBy(AbstractAuthenticationSession.class)
	interface AuthenticationSession extends AutoCloseable {
		
		/**
		 * Fetch user details for a user that has to be created in the database.
		 * 
		 * @param user
		 * 		{@link User} object to be synchronized
		 * @throws IOException
		 * 		When the user couldn't be fetched.
		 */
		void fetch(User user) throws IOException;
		
		/**
		 * Synchronize a User with the directory.
		 * 
		 * @param user
		 * 		{@link User} object to be synchronized
		 * @return
		 * 		true if the {@link User} object has been updated
		 * @throws IOException
		 * 		When the synchronization failed.
		 */
		boolean synchronize(User user) throws IOException;
		
		@Override
		void close() throws IOException;
		
	}
	
	/**
	 * Basic implementation for {@link AuthenticationSession} providing no-op
	 * behavior for the methods.
	 * 
	 * @author Jan-Willem
	 *
	 */
	static class AbstractAuthenticationSession implements AuthenticationSession {

		@Override
		public void fetch(User user) throws IOException {}

		@Override
		public boolean synchronize(User user) throws IOException {
			return false;
		}

		@Override
		public void close() throws IOException {}
		
	}
	
	/**
	 * If the {@code AuthenticationProvider} is unavailable or an unexpected
	 * error occurred.
	 * 
	 * @author Jan-Willem
	 *
	 */
	static class AuthenticationProviderUnavailable extends Exception {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = -1208837406761307088L;
		
		public AuthenticationProviderUnavailable(Throwable throwable) {
			super(throwable);
		}
		
		public AuthenticationProviderUnavailable(String message) {
			super(message);
		}
		
	}
	
	/**
	 * If the supplied credentials are invalid.
	 * 
	 * @author Jan-Willem
	 *
	 */
	static class InvalidCredentialsException extends Exception {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = -4010638096567999252L;
		
	}
	
}
