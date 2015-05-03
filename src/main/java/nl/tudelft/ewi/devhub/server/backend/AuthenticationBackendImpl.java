package nl.tudelft.ewi.devhub.server.backend;

import java.io.IOException;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationProvider.AuthenticationProviderUnavailable;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationProvider.AuthenticationSession;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationProvider.InvalidCredentialsException;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Default implementation of the {@link AuthenticationBackend}.
 * Authenticates through the {@link #authenticationProvider} and if authentication is succesful
 * obtains the user from the {@link #usersProvider database}.
 * 
 * @author Michael
 *
 */
@Slf4j
@Singleton
public class AuthenticationBackendImpl implements AuthenticationBackend {
	
	/**
	 * The database that stores all users.
	 */
	private final Provider<Users> usersProvider;
	
	/**
	 * Verifies that the provided credentials are valid.
	 */
	private final Provider<AuthenticationProvider> authenticationProvider;
	
	@Inject
	AuthenticationBackendImpl(Provider<Users> usersProvider, Provider<AuthenticationProvider> authenticationProvider) {
		this.usersProvider = usersProvider;
		this.authenticationProvider = authenticationProvider;
	}

	private void createNewUser(String netId, String password,
			AuthenticationSession session, Users database) throws IOException {
		User user = new User();
		user.setNetId(netId);
		user.setPassword(password);
		
		session.fetch(user);
		database.persist(user);
	}
	
	private void obtainUser(String netId, String password,
			AuthenticationSession session) throws IOException {
		Users database = usersProvider.get();
		User user;
		
		try {
			user = database.findByNetId(netId);
			
			if (session.synchronize(user)) {
				database.merge(user);
			}
		}
		catch (EntityNotFoundException e) {
			log.info("Persisting user: {} since he/she is not yet present in the database", netId);
			
			createNewUser(netId, password, session, database);
		}
	}
	
	@Override
	public boolean authenticate(String netId, String password) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(netId));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(password));
		
		try (AuthenticationSession session = authenticationProvider.get()
				.authenticate(netId, password)) {
			
			obtainUser(netId, password, session);
			
			return true;
		}
		catch (InvalidCredentialsException e) {
			log.trace("Invalid credentials for user with netId {}", netId);
		}
		catch (IOException | AuthenticationProviderUnavailable e) {
			log.info(e.getMessage(), e);
		}
		
		return false;
	}

}
