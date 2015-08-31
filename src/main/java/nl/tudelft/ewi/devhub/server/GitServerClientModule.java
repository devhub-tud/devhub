package nl.tudelft.ewi.devhub.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.GitServerClientImpl;
import nl.tudelft.ewi.git.client.Groups;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Users;

/**
 * Dependency injection module for the GitServerClient.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class GitServerClientModule extends AbstractModule {

	private final Config config;

	public GitServerClientModule(Config config) {
		this.config = config;
	}

	@Override
	protected void configure() {
		bind(GitServerClient.class).toInstance(new GitServerClientImpl(config.getGitServerHost()));
	}

	@Provides
	public Users users(GitServerClient gitServerClient) {
		return gitServerClient.users();
	}

	@Provides
	public Groups groups(GitServerClient gitServerClient) {
		return gitServerClient.groups();
	}

	@Provides
	public Repositories repositories(GitServerClient gitServerClient) {
		return gitServerClient.repositories();
	}

}
