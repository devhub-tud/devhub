package nl.tudelft.ewi.devhub.server.backend;

public interface AuthenticationBackend {
	
	public boolean authenticate(String netId, String password);

}
