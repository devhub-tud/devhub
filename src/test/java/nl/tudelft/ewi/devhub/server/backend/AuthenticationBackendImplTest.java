package nl.tudelft.ewi.devhub.server.backend;

import nl.tudelft.ewi.devhub.server.backend.AuthenticationProvider.AuthenticationProviderUnavailable;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationProvider.AuthenticationSession;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationProvider.InvalidCredentialsException;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AuthenticationBackendImplTest {
	
	private final Users users = mock(Users.class);
	private final MockedAuthenticationProvider authenticationProvider = new MockedAuthenticationProvider();
	
	private final AuthenticationBackendImpl backend = new AuthenticationBackendImpl(
			new ValueProvider<Users>(users),
			new ValueProvider<AuthenticationProvider>(authenticationProvider));
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidNetId() {
		backend.authenticate(null, "test");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidPassword() {
		backend.authenticate("test", null);
	}
	
	private final static String username = "username";
	private final static String password = "password";
	
	@Test
	public void testAuthenticate() {
		AuthenticationSession mockedAuthenticationSession = mock(AuthenticationSession.class);
		authenticationProvider.setSession(mockedAuthenticationSession);
		assertTrue(backend.authenticate(username, password));
		verify(users).findByNetId(username);
	}
	
	@Test
	public void testAuthenticationProviderUnavailable() {
		authenticationProvider.setAuthenticationProviderUnavailable(new AuthenticationProviderUnavailable(""));
		assertFalse(backend.authenticate(username, password));
	}
	
	@Test
	public void testInvalidCredentialsException() {
		authenticationProvider.setInvalidCredentialsException(new InvalidCredentialsException());
		assertFalse(backend.authenticate(username, password));
	}

}
