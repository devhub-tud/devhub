package nl.tudelft.ewi.devhub.server;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import java.io.File;
import java.lang.annotation.Annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.AuthenticationBackend;
import nl.tudelft.ewi.devhub.server.backend.LdapBackend;
import nl.tudelft.ewi.devhub.server.backend.LdapBackend.LdapUserProcessor;
import nl.tudelft.ewi.devhub.server.backend.LdapBackend.PersistingLdapUserProcessor;
import nl.tudelft.ewi.devhub.server.database.DbModule;
import nl.tudelft.ewi.devhub.server.web.templating.TranslatorFactory;
import nl.tudelft.ewi.git.client.GitServerClient;
import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;
import org.reflections.Reflections;

@Slf4j
public class DevhubModule extends AbstractModule {
	
	private final File rootFolder;
	private final Config config;

	public DevhubModule(Config config, File rootFolder) {
		this.config = config;
		this.rootFolder = rootFolder;
	}

	@Override
	protected void configure() {
		install(new DbModule());
		install(new RequestScopeModule());
		install(new JaxrsModule());
		
		requireBinding(ObjectMapper.class);
		
		bind(File.class).annotatedWith(Names.named("directory.templates")).toInstance(new File(rootFolder, "templates"));
		bind(TranslatorFactory.class).toInstance(new TranslatorFactory("i18n.devhub"));
		bind(Config.class).toInstance(config);
		
		bind(GitServerClient.class).toInstance(new GitServerClient(config.getGitServerHost()));
		
		bind(AuthenticationBackend.class).to(LdapBackend.class);
		bind(LdapUserProcessor.class).to(PersistingLdapUserProcessor.class);
		
		findResourcesWith(Path.class);
		findResourcesWith(Provider.class);
	}

	private void findResourcesWith(Class<? extends Annotation> ann) {
		Reflections reflections = new Reflections(getClass().getPackage().getName());
		for (Class<?> clasz : reflections.getTypesAnnotatedWith(ann)) {
			log.info("Registering resource {}", clasz);
			bind(clasz);
		}
	}

}
