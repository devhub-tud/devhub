package nl.devhub.client;

import java.lang.annotation.Annotation;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;
import nl.devhub.client.settings.Settings;

import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;
import org.reflections.Reflections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;

@Slf4j
public class ClientModule extends AbstractModule {
	
	private final Settings settings;

	public ClientModule(Settings settings) {
		this.settings = settings;
	}

	@Override
	protected void configure() {
		install(new RequestScopeModule());
		install(new JaxrsModule());
		
		requireBinding(ObjectMapper.class);
		bind(Settings.class).toInstance(settings);
		
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
