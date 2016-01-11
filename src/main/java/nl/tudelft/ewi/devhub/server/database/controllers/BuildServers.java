package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.BuildServer;
import nl.tudelft.ewi.devhub.server.database.entities.QBuildServer;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;

public class BuildServers extends Controller<BuildServer> {

	@Inject
	public BuildServers(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public List<BuildServer> listAll() {
		return query().from(QBuildServer.buildServer)
				.orderBy(QBuildServer.buildServer.name.toLowerCase().asc())
				.list(QBuildServer.buildServer);
	}

	@Transactional
	public BuildServer findById(long id) {
		BuildServer buildServer = query().from(QBuildServer.buildServer)
				.where(QBuildServer.buildServer.id.eq(id))
				.singleResult(QBuildServer.buildServer);
		
		if (buildServer == null) {
			throw new EntityNotFoundException();
		}
		return buildServer;
	}

	@Transactional
	public BuildServer findByCredentials(String name, String secret) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(secret);
		
		BuildServer buildServer = query().from(QBuildServer.buildServer)
				.where(QBuildServer.buildServer.name.equalsIgnoreCase(name))
				.where(QBuildServer.buildServer.secret.eq(secret))
				.singleResult(QBuildServer.buildServer);
		
		if (buildServer == null) {
			throw new EntityNotFoundException();
		}
		return buildServer;
	}

}
