package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.BuildServer;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class BuildServersTest {
	
	@Inject
	private Random random;
	
	@Inject
	private BuildServers buildServers;
	
	@Test(expected=ConstraintViolationException.class)
	public void testCreateBuildServerWithoutHost() {
		BuildServer buildServer = new BuildServer();
		buildServer.setName(randomString());
		buildServer.setSecret(randomString());
		buildServers.persist(buildServer);
	}
	
	@Test(expected=ConstraintViolationException.class)
	public void testCreateBuildServerWithoutName() {
		BuildServer buildServer = new BuildServer();
		buildServer.setHost(randomString());
		buildServer.setSecret(randomString());
		buildServers.persist(buildServer);
	}
	
	@Test(expected=ConstraintViolationException.class)
	public void testCreateBuildServerWithoutSectret() {
		BuildServer buildServer = new BuildServer();
		buildServer.setHost(randomString());
		buildServer.setName(randomString());
		buildServers.persist(buildServer);
	}
	
	@Test
	public void testCreateBuildServer() {
		BuildServer buildServer = createBuildServer();
		buildServers.persist(buildServer);
	}
	
	@Test
	public void testFetchBuildServer() {
		BuildServer buildServer = createBuildServer();
		buildServers.persist(buildServer);
		assertEquals(buildServer, buildServers.findById(buildServer.getId()));
	}
	
	@Test
	public void testFetchBuildServerByCreds() {
		BuildServer buildServer = createBuildServer();
		buildServers.persist(buildServer);
		assertEquals(buildServer, buildServers.findByCredentials(
				buildServer.getName(), buildServer.getSecret()));
	}
	
	public BuildServer createBuildServer() {
		BuildServer buildServer = new BuildServer();
		buildServer.setHost(randomString());
		buildServer.setName(randomString());
		buildServer.setSecret(randomString());
		return buildServer;
	}
	
	protected String randomString() {
		return new BigInteger(130, random).toString(32);
	}

}
