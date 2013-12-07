package nl.devhub.server;

import java.io.File;
import java.lang.annotation.Annotation;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;
import nl.devhub.server.database.DbModule;
import nl.devhub.server.web.templating.TranslatorFactory;

import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;
import org.reflections.Reflections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

@Slf4j
public class DevhubModule extends AbstractModule {
	
	private final File rootFolder;

	public DevhubModule(File rootFolder) {
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
