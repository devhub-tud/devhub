package nl.devhub.client;

import lombok.extern.slf4j.Slf4j;
import nl.devhub.client.docker.DockerManager;
import nl.devhub.client.docker.Job;
import nl.devhub.client.docker.Logger;
import nl.devhub.client.docker.settings.Settings;
import nl.devhub.client.guice.DevhubClientModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

@Slf4j
public class Test {

	public static void main(String[] args) throws InterruptedException {

		Settings settings = new Settings();
		Injector injector = Guice.createInjector(new DevhubClientModule(settings));
		
		settings.getMaxConcurrentContainers().setValue(1);
		settings.getHost().setValue("http://192.168.178.36:4243");
		
		Job job = new Job(
				"devhub/builder:latest", 
				"/workspace", 
				new String[] { "mvn", "clean", "package" }, 
				ImmutableMap.of("/workspace", "/workspace"), 
				new Logger() {
					@Override
					public void onNextLine(String line) {
						log.info(line);
					}

					@Override
					public void onClose() {
						log.info("Stream closed");
					}
				}
		);
		
		DockerManager manager = injector.getInstance(DockerManager.class);
		manager.run(job);
		
	}

}
