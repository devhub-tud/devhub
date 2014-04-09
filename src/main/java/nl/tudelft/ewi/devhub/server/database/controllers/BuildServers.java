package nl.tudelft.ewi.devhub.server.database.controllers;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.NotFoundException;

import nl.tudelft.ewi.devhub.server.database.entities.BuildServer;
import nl.tudelft.ewi.devhub.server.database.entities.QBuildServer;

import com.google.inject.persist.Transactional;

public class BuildServers extends Controller<BuildServer> {

	@Inject
	public BuildServers(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public List<BuildServer> listAll() {
		return query().from(QBuildServer.buildServer)
				.orderBy(QBuildServer.buildServer.name.asc())
				.list(QBuildServer.buildServer);
	}

	@Transactional
	public BuildServer findById(long id) {
		BuildServer buildServer = query().from(QBuildServer.buildServer)
				.where(QBuildServer.buildServer.id.eq(id))
				.singleResult(QBuildServer.buildServer);
		
		if (buildServer == null) {
			throw new NotFoundException();
		}
		return buildServer;
	}

	@Transactional
	public BuildServer findByCredentials(String name, String secret) {
		BuildServer buildServer = query().from(QBuildServer.buildServer)
				.where(QBuildServer.buildServer.name.eq(name))
				.where(QBuildServer.buildServer.secret.eq(secret))
				.singleResult(QBuildServer.buildServer);
		
		if (buildServer == null) {
			throw new NotFoundException();
		}
		return buildServer;
	}

}
