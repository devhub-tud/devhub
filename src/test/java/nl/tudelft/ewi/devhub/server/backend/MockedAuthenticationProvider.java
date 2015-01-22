package nl.tudelft.ewi.devhub.server.backend;

class MockedAuthenticationProvider implements AuthenticationProvider {
	
	private AuthenticationProviderUnavailable authenticationProviderUnavailable;
	private InvalidCredentialsException invalidCredentialsException;
	private AuthenticationSession session;

	@Override
	public AuthenticationSession authenticate(String username,
			String password) throws AuthenticationProviderUnavailable,
			InvalidCredentialsException {
		if(authenticationProviderUnavailable != null) {
			throw authenticationProviderUnavailable;
		}
		else if (invalidCredentialsException != null) {
			throw invalidCredentialsException;
		}
		else {
			return session;
		}
	}

	public AuthenticationProviderUnavailable getAuthenticationProviderUnavailable() {
		return authenticationProviderUnavailable;
	}

	public void setAuthenticationProviderUnavailable(
			AuthenticationProviderUnavailable authenticationProviderUnavailable) {
		this.authenticationProviderUnavailable = authenticationProviderUnavailable;
		this.invalidCredentialsException = null;
	}

	public InvalidCredentialsException getInvalidCredentialsException() {
		return invalidCredentialsException;
	}

	public void setInvalidCredentialsException(
			InvalidCredentialsException invalidCredentialsException) {
		this.invalidCredentialsException = invalidCredentialsException;
		this.authenticationProviderUnavailable = null;
	}

	public AuthenticationSession getSession() {
		return session;
	}

	public void setSession(AuthenticationSession session) {
		this.invalidCredentialsException = null;
		this.authenticationProviderUnavailable = null;
		this.session = session;
	}
	
}