package nl.tudelft.ewi.devhub.server.backend;


import nl.tudelft.ewi.build.client.BuildServerBackend;
import nl.tudelft.ewi.build.jaxrs.models.BuildRequest;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend.BuildSubmitter;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildServers;
import nl.tudelft.ewi.devhub.server.database.entities.BuildServer;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.persist.UnitOfWork;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class BuildsBackendTest {

	private static Config config = mock(Config.class);
	private static BuildResults buildResults = mock(BuildResults.class);
	private static BuildServers buildServers = mock(BuildServers.class);
	private static BuildServerBackend buildBackend = mock(BuildServerBackend.class);
	private static BuildServer buildServer;
	
	@BeforeClass
	public static void setupBuildServers() {
		buildServer = new BuildServer();
		buildServer.setId(1l);
		buildServer.setHost("host");
		buildServer.setName("buildname");
		buildServer.setSecret("secret");
		
		when(buildServers.listAll()).thenReturn(Lists.newArrayList(buildServer));
		when(buildServers.findByCredentials("buildname", "secret")).thenReturn(buildServer);
		when(buildServers.findById(1l)).thenReturn(buildServer);
		when(buildBackend.offerBuildRequest(any(BuildRequest.class))).thenReturn(true);
	}
	
	private BuildsBackend buildsBackend = new BuildsBackend(buildServers,
			new ValueProvider<BuildSubmitter>(new MockedBuildSubmitter()), buildResults, config);
	
	@Test
	public void testListActiveBuildServers() {
		assertEquals(buildServers.listAll(),
			buildsBackend.listActiveBuildServers());
	}
	
	@Test
	public void testAuthenticate() {
		assertTrue(buildsBackend.authenticate(buildServer.getName(),
				buildServer.getSecret()));
	}
	
	@Test
	public void testAddBuildServer() throws ApiError {
		buildsBackend.addBuildServer(buildServer);
		verify(buildServers).persist(buildServer);
	}
	
	@Test
	public void testDeleteBuildServer() throws ApiError {
		buildsBackend.deleteBuildServer(buildServer.getId());
		verify(buildServers).delete(buildServer);
	}
	
	@Test
	public void testOfferBuild() throws InterruptedException {
		BuildRequest buildRequest = new BuildRequest();
		buildsBackend.offerBuild(buildRequest);
		Thread.sleep(100);
		verify(buildBackend).offerBuildRequest(buildRequest);
	}
	
	@After
	public void shutdown() throws InterruptedException {
		buildsBackend.shutdown();
	}

	static class MockedBuildSubmitter extends BuildSubmitter {
	
		MockedBuildSubmitter() {
			super(new ValueProvider<BuildServers>(buildServers),
					new ValueProvider<UnitOfWork>(mock(UnitOfWork.class)));
		}
		
		@Override
		protected BuildServerBackend createBuildServerBackend(String host,
				String name, String secret) {
			return buildBackend;
		}
		
	}
	
}
