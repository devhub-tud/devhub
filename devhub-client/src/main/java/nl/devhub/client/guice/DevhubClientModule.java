package nl.devhub.client.guice;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.devhub.client.docker.settings.Settings;

import com.google.inject.AbstractModule;

@Data
@EqualsAndHashCode(callSuper = false)
public class DevhubClientModule extends AbstractModule {

	private final Settings settings;
	
	@Override
	protected void configure() {
		bind(Settings.class).toInstance(settings);
	}

}
