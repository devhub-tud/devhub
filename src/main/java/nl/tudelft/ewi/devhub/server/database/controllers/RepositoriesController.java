package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;

import static nl.tudelft.ewi.devhub.server.database.entities.QRepositoryEntity.repositoryEntity;

import javax.persistence.EntityManager;

/**
 * Created by Jan-Willem on 8/15/2015.
 */
public class RepositoriesController extends Controller<RepositoryEntity> {

	@Inject
	public RepositoriesController(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public RepositoryEntity find(final String repositoryName) {
		return ensureNotNull(query().from(repositoryEntity)
			.where(repositoryEntity.repositoryName.equalsIgnoreCase(repositoryName))
			.singleResult(repositoryEntity), String.format("No repository found for %s", repositoryName));
	}

}
