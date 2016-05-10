package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import nl.tudelft.ewi.build.client.BuildServerBackend;
import nl.tudelft.ewi.build.client.MockedBuildServerBackend;
import nl.tudelft.ewi.devhub.modules.MockedGitoliteGitServerModule;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.Bootstrapper;
import nl.tudelft.ewi.devhub.server.backend.MockedAuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.MockedMailBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.BuildResultMailer;
import nl.tudelft.ewi.devhub.server.backend.mail.MailBackend;
import nl.tudelft.ewi.devhub.server.backend.warnings.CommitPushWarningGenerator;
import nl.tudelft.ewi.devhub.server.database.controllers.TestDatabaseModule;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.devhub.server.web.resources.HooksResource.GitPushHandlerWorkerFactory;
import nl.tudelft.ewi.devhub.server.web.resources.HooksResourceTest.HooksResourceTestModule;
import nl.tudelft.ewi.devhub.webtests.rules.ExecutorServiceRule;
import nl.tudelft.ewi.devhub.webtests.rules.UnitOfWorkRule;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@RunWith(JukitoRunner.class)
@UseModules(HooksResourceTestModule.class)
public class HooksResourceTest {

    @Inject GitPushHandlerWorkerFactory gitPushHandlerWorkerFactory;
    @Inject Provider<Bootstrapper> bootstrapperProvider;
    @Inject Provider<RepositoriesApi> repositoriesApiProvider;

    @Inject @Rule public UnitOfWorkRule unitOfWorkRule;
    @ClassRule public static ExecutorServiceRule executorServiceRule = new ExecutorServiceRule();

    public static class HooksResourceTestModule extends AbstractModule {

        @Override
        protected void configure() {
            install(new TestDatabaseModule());
            install(new MockedGitoliteGitServerModule());
            bind(AuthenticationBackend.class).to(MockedAuthenticationBackend.class);
            bind(BuildServerBackend.class).to(MockedBuildServerBackend.class);
            bind(MockedBuildServerBackend.class).toInstance(new MockedBuildServerBackend(null, null));
            bind(MailBackend.class).to(MockedMailBackend.class);
            bind(BuildResultMailer.class).toInstance(mock(BuildResultMailer.class));

            install(new FactoryModuleBuilder()
                .implement(HooksResource.GitPushHandlerWorker.class, HooksResource.GitPushHandlerWorker.class)
                .build(GitPushHandlerWorkerFactory.class));

            bind(Key.get(new TypeLiteral<Set<CommitPushWarningGenerator>>() {})).toInstance(Collections.emptySet());
        }

        @Provides
        @Singleton
        public ExecutorService getExecutorService() {
            return executorServiceRule.getExecutorService();
        }
    }

    @Before
    public void provisionEnvironment() throws IOException, ApiError {
        bootstrapperProvider.get().prepare("/simple-environment.json");
    }

    @Test
    public void testHooksResource() throws ExecutionException, InterruptedException {
        Collection<RepositoryModel> repositories = repositoriesApiProvider.get().listAllRepositories();
        val gitPush = new GitPush();
        gitPush.setRepository(repositories.stream().findFirst().get().getName());

        executorServiceRule.getExecutorService()
            .submit(gitPushHandlerWorkerFactory.create(gitPush))
            .get();
    }
}
