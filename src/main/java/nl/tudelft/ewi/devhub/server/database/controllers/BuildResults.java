package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Map;

import static nl.tudelft.ewi.devhub.server.database.entities.QBuildResult.buildResult;

public class BuildResults extends Controller<BuildResult> {

	@Inject
	public BuildResults(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public BuildResult find(Commit commit) {
		return find(commit.getRepository(), commit.getCommitId());
	}

	@Transactional
	public BuildResult find(RepositoryEntity repository, String commitId) {
		Preconditions.checkNotNull(repository);
		Preconditions.checkNotNull(commitId);
		
		BuildResult result = query().from(buildResult)
				.where(buildResult.commit.repository.id.eq(repository.getId()))
				.where(buildResult.commit.commitId.equalsIgnoreCase(commitId))
				.singleResult(buildResult);

		if (result == null) {
			throw new EntityNotFoundException();
		}
		return result;
	}

	@Transactional
	public Map<String, BuildResult> findBuildResults(RepositoryEntity repository, Collection<String> commitIds) {
		return query().from(buildResult)
				.where(buildResult.commit.repository.id.eq(repository.getId())
						.and(buildResult.commit.commitId.in(commitIds)))
				.map(buildResult.commit.commitId, buildResult);
	}

	@Transactional
	public boolean exists(Commit commit) {
		return exists(commit.getRepository(), commit.getCommitId());
	}
	
	@Transactional
	public boolean exists(RepositoryEntity repository, String commitId) {
		Preconditions.checkNotNull(repository);
		Preconditions.checkNotNull(commitId);
		
		try {
			find(repository, commitId);
			return true;
		}
		catch (EntityNotFoundException e) {
			return false;
		}
	}
	
}
