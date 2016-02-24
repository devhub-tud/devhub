package nl.tudelft.ewi.devhub.modules;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.backend.JGitRepositoryFacadeFactory;
import nl.tudelft.ewi.git.backend.RepositoryFacadeFactory;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.web.MockedSingleton;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.BranchApiImpl;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.CommitApiImpl;
import nl.tudelft.ewi.git.web.api.GroupApi;
import nl.tudelft.ewi.git.web.api.GroupApiImpl;
import nl.tudelft.ewi.git.web.api.GroupsApi;
import nl.tudelft.ewi.git.web.api.GroupsApiImpl;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApiImpl;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import nl.tudelft.ewi.git.web.api.RepositoryApiImpl;
import nl.tudelft.ewi.git.web.api.Transformers;
import nl.tudelft.ewi.git.web.api.UsersApi;
import nl.tudelft.ewi.git.web.api.UsersApiImpl;
import nl.tudelft.ewi.git.web.api.di.BranchApiFactory;
import nl.tudelft.ewi.git.web.api.di.CommitApiFactory;
import nl.tudelft.ewi.git.web.api.di.Factory;
import nl.tudelft.ewi.git.web.api.di.GroupApiFactory;
import nl.tudelft.ewi.git.web.api.di.RepositoryApiFactory;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.config.Config;
import nl.tudelft.ewi.gitolite.config.ConfigImpl;
import nl.tudelft.ewi.gitolite.git.GitException;
import nl.tudelft.ewi.gitolite.git.GitManager;
import nl.tudelft.ewi.gitolite.keystore.KeyStore;
import nl.tudelft.ewi.gitolite.keystore.KeyStoreImpl;
import nl.tudelft.ewi.gitolite.repositories.PathRepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Valid;
import javax.ws.rs.InternalServerErrorException;
import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.when;

/**
 * Mock out the Gitolite manager components, so we are not dependent on a Gitolite installation.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class MockedGitoliteGitServerModule extends AbstractModule {

	private File adminFolder = Files.createTempDir();
	private File configFolder = ensureExists(new File(adminFolder, "conf"));
	private File keyDir = ensureExists(new File(adminFolder, "keydir"));
	private File mirrorsFolder = Files.createTempDir();
	private File repositoriesFolder = Files.createTempDir();

	@Spy
	private KeyStore keyStore = new KeyStoreImpl(keyDir);
	@Spy
	private GitManager gitManager = new MockedGitManager();
	@Spy
	private Config gitoliteConfig = new ConfigImpl();
	@InjectMocks
	private ManagedConfig managedConfig;
	@Mock
	private nl.tudelft.ewi.git.Config configuration;

	@Override
	protected void configure() {
		MockitoAnnotations.initMocks(this);
		createMockedMirrorsFolder();
		createMockedRepositoriesFolder();
		createMockedGitoliteManagerRepo();

		bind(UsersApi.class).to(UsersApiImpl.class);
		bind(GroupsApi.class).to(GroupsApiImpl.class);
		bind(RepositoriesApi.class).to(FakeRepoositoriesApi.class);

		bindSubResourceFactory(GroupApi.class, GroupApiImpl.class, GroupApiFactory.class);
		bindSubResourceFactory(CommitApi.class, CommitApiImpl.class, CommitApiFactory.class);
		bindSubResourceFactory(BranchApi.class, BranchApiImpl.class, BranchApiFactory.class);
		bindSubResourceFactory(RepositoryApi.class, RepositoryApiImpl.class, RepositoryApiFactory.class);

		bind(RepositoryFacadeFactory.class).to(JGitRepositoryFacadeFactory.class);

		bind(ManagedConfig.class).toInstance(managedConfig);
		bind(nl.tudelft.ewi.git.Config.class).toInstance(configuration);

		// Bind GitManager and Config spies so tests can verify on them
		bind(GitManager.class).annotatedWith(MockedSingleton.class).toInstance(gitManager);
		bind(Config.class).annotatedWith(MockedSingleton.class).toInstance(gitoliteConfig);
		bind(KeyStore.class).annotatedWith(MockedSingleton.class).toInstance(keyStore);
		bind(RepositoriesManager.class).to(PathRepositoriesManager.class);
		bind(PathRepositoriesManager.class).toInstance(new PathRepositoriesManager(repositoriesFolder));
		// Bind folders so tests can prepare them
		bind(File.class).annotatedWith(Names.named("mirrors.folder")).toInstance(mirrorsFolder);
		bind(File.class).annotatedWith(Names.named("repositories.folder")).toInstance(repositoriesFolder);

		// Clean up folders on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(this::removeFolders));
	}

	protected void createMockedMirrorsFolder() {
		String mirrorsPath = mirrorsFolder.toPath().toString() + "/";
		when(configuration.getMirrorsDirectory()).thenReturn(mirrorsFolder);
		log.info("Initialized mirrors folder in {}", mirrorsPath);
	}

	protected void createMockedRepositoriesFolder() {
		String repositoriesPath = repositoriesFolder.toPath().toString() + "/";
		when(configuration.getGitoliteBaseUrl()).thenReturn(repositoriesPath);
		when(configuration.getRepositoriesDirectory()).thenReturn(repositoriesFolder);
		log.info("Initialized bare repository folder in {}", repositoriesPath);
	}

	protected <T> void bindSubResourceFactory(Class<T> iface, Class<? extends T> implementation, Class<? extends Factory<T>> factory) {
		log.info("Registering sub-resource {}", implementation);
		install(new FactoryModuleBuilder()
			.implement(iface, implementation)
			.build(factory));
	}

	@SneakyThrows
	protected void createMockedGitoliteManagerRepo() {
		File config = new File(configFolder, "gitolite.conf");
		Files.createParentDirs(config);
	}

	@SneakyThrows
	private void removeFolders() {
		FileUtils.deleteDirectory(repositoriesFolder);
		FileUtils.deleteDirectory(adminFolder);
		FileUtils.deleteDirectory(mirrorsFolder);
	}

	/**
	 * Instead of stubbing the admin folder, we spy a custom implementation, so
	 * users can still reset the mock.
	 */
	public class MockedGitManager implements GitManager {

		@Override
		public File getWorkingDirectory() {
			return adminFolder;
		}

		@Override public boolean exists(){ return true; }
		@Override public void open() {}
		@Override public void remove(String filePattern) throws IOException, GitException, InterruptedException {}
		@Override public void clone(String uri) throws IOException, InterruptedException, GitException { }
		@Override public void init() throws IOException, InterruptedException, GitException { }
		@Override public boolean pull() throws IOException, InterruptedException, GitException {return false; }
		@Override public void commitChanges() throws InterruptedException, IOException, GitException {}
		@Override public void push() throws IOException, InterruptedException, GitException {}

	}

	/**
	 * Gitolite initializes bare repositories on first use, we need to hook on the repository creation to
	 * initialize a bare repository.
 	 */
	public static class FakeRepoositoriesApi extends RepositoriesApiImpl {

		private final File repositoriesFolder;

		@Inject
		public FakeRepoositoriesApi(@Named("repositories.folder") File repositoriesFolder, Transformers transformers, RepositoriesManager repositoriesManager, RepositoryApiFactory repositoryApiFactory, ManagedConfig managedConfig, nl.tudelft.ewi.git.Config config) {
			super(transformers, repositoriesManager, repositoryApiFactory, managedConfig, config);
			this.repositoriesFolder = repositoriesFolder;
		}

		@Override
		public DetailedRepositoryModel createRepository(@Valid CreateRepositoryModel createRepositoryModel) throws InternalServerErrorException {
			try {
				Git.init()
					.setBare(true)
					.setDirectory(new File(repositoriesFolder, createRepositoryModel.getName() + ".git"))
					.call();
				return super.createRepository(createRepositoryModel);
			}
			catch (GitAPIException e) {
				throw new InternalServerErrorException(e.getMessage(), e);
			}
		}

	}

	@SneakyThrows
	static File ensureExists(File file) {
		FileUtils.forceMkdir(file);
		return file;
	}

}
