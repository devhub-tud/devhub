package nl.tudelft.ewi.devhub.server;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackendImpl;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationProvider;
import nl.tudelft.ewi.devhub.server.backend.LdapAuthenticationProvider;
import nl.tudelft.ewi.devhub.server.backend.LdapBackend.LdapUserProcessor;
import nl.tudelft.ewi.devhub.server.backend.LdapBackend.PersistingLdapUserProcessor;
import nl.tudelft.ewi.devhub.server.backend.warnings.CommitPushWarningGenerator;
import nl.tudelft.ewi.devhub.server.database.DbModule;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.filters.RepositoryAuthorizeFilter;
import nl.tudelft.ewi.devhub.server.web.filters.UserAuthorizeFilter;
import nl.tudelft.ewi.devhub.server.web.templating.TranslatorFactory;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.GitServerClientImpl;
import nl.tudelft.ewi.git.client.Repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistFilter;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;

import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.reflections.Reflections;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.lang.annotation.Annotation;

@Slf4j
public class DevhubModule extends ServletModule {

	private final File rootFolder;
	private final Config config;

	public DevhubModule(Config config, File rootFolder) {
		this.config = config;
		this.rootFolder = rootFolder;
	}

	@Override
	protected void configureServlets() {
		install(new DbModule());
		install(new JaxrsModule());
		bind(JacksonJaxbXMLProvider.class);
		requireBinding(ObjectMapper.class);

		bind(File.class).annotatedWith(Names.named("directory.templates")).toInstance(new File(rootFolder, "templates"));
		bind(TranslatorFactory.class).toInstance(new TranslatorFactory("i18n.devhub"));
		bind(Config.class).toInstance(config);

		bind(GitServerClient.class).toInstance(new GitServerClientImpl(config.getGitServerHost()));
		bind(AuthenticationBackend.class).to(AuthenticationBackendImpl.class);
		bind(AuthenticationProvider.class).to(LdapAuthenticationProvider.class);
		bind(LdapUserProcessor.class).to(PersistingLdapUserProcessor.class);
		bindWarningGenerators();

		filter("/*").through(PersistFilter.class);
		filter("/accounts*", "/build-servers*", "/projects*", "/validation*", "/courses*").through(UserAuthorizeFilter.class);
		filterRegex("^/courses/[^/]+/[^/]+/groups/\\d+(/.*)?").through(RepositoryAuthorizeFilter.class);

		findResourcesWith(Path.class);
		findResourcesWith(Provider.class);
	}

	private void bindWarningGenerators() {
		Multibinder<CommitPushWarningGenerator> uriBinder = Multibinder.newSetBinder(binder(), CommitPushWarningGenerator.class);
		Reflections reflections = new Reflections(CommitPushWarningGenerator.class.getPackage().getName());
		for (Class<? extends CommitPushWarningGenerator> clasz : reflections.getSubTypesOf(CommitPushWarningGenerator.class)) {
			log.info("Registering Push warning generator {}", clasz);
			uriBinder.addBinding().to(clasz);
		}
	}

	private void findResourcesWith(Class<? extends Annotation> ann) {
		Reflections reflections = new Reflections(getClass().getPackage().getName());
		for (Class<?> clasz : reflections.getTypesAnnotatedWith(ann)) {
			log.info("Registering resource {}", clasz);
			bind(clasz);
		}
	}

	@Provides
	public Repositories provideRepositories(final GitServerClient gitServerClient) {
		return gitServerClient.repositories();
	}
	
	@Provides
	@Named("current.user")
	@RequestScoped
	public User provideCurrentUser(HttpServletRequest request, Users users) throws UnauthorizedException {
        HttpSession session = request.getSession(false);
        if(session != null) {
            try {
                String netId = request.getSession().getAttribute("netID").toString();
                return users.findByNetId(netId);
            }
            catch (EntityNotFoundException e) {
                throw new UnauthorizedException();
            }
        }
        throw new UnauthorizedException();
	}

	@Provides
	@Named("current.group")
	@RequestScoped
	public Group provideCurrentGroup() {
		throw new IllegalStateException("Group must be manually seeded");
	}

}
