package nl.tudelft.ewi.devhub.server.backend;


import nl.tudelft.ewi.build.client.BuildServerBackend;
import nl.tudelft.ewi.build.jaxrs.models.BuildRequest;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend.BuildSubmitter;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildServers;
import nl.tudelft.ewi.devhub.server.database.entities.BuildServer;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;

import com.google.common.collect.Lists;
import com.google.inject.persist.UnitOfWork;

import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BuildsBackendTest {

	@Mock Config config;
	@Mock BuildResults buildResults;
	@Mock	BuildServers buildServers;
	@Mock BuildServerBackend buildBackend;
	@Mock RepositoriesApi repositoriesApi;
	private static BuildServer buildServer = createBuildServer();
	private BuildsBackend buildsBackend;

	private static BuildServer createBuildServer() {
		BuildServer buildServer = new BuildServer();
		buildServer.setId(1l);
		buildServer.setHost("host");
		buildServer.setName("buildname");
		buildServer.setSecret("secret");
		return buildServer;
	}

	@Before
	public void setupBuildServers() {
		when(buildServers.listAll()).thenReturn(Lists.newArrayList(buildServer));
		when(buildServers.findByCredentials("buildname", "secret")).thenReturn(buildServer);
		when(buildServers.findById(1l)).thenReturn(buildServer);
		when(buildBackend.offerBuildRequest(Mockito.any(BuildRequest.class))).thenReturn(true);
		buildsBackend = new BuildsBackend(buildServers,
			new ValueProvider<BuildSubmitter>(new MockedBuildSubmitter()), buildResults, repositoriesApi, config, Executors.newSingleThreadExecutor());
	}


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

	class MockedBuildSubmitter extends BuildSubmitter {
	
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
