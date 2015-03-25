package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.QBuildResult;

public class BuildResults extends Controller<BuildResult> {

	@Inject
	public BuildResults(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public BuildResult find(Group group, String commitId) {
		Preconditions.checkNotNull(group);
		Preconditions.checkNotNull(commitId);
		
		BuildResult result = query().from(QBuildResult.buildResult)
				.where(QBuildResult.buildResult.repository.groupId.eq(group.getGroupId()))
				.where(QBuildResult.buildResult.commitId.equalsIgnoreCase(commitId))
				.singleResult(QBuildResult.buildResult);
		
		if (result == null) {
			throw new EntityNotFoundException();
		}
		return result;
	}
	
	@Transactional
	public boolean exists(Group group, String commitId) {
		Preconditions.checkNotNull(group);
		Preconditions.checkNotNull(commitId);
		
		try {
			find(group, commitId);
			return true;
		}
		catch (EntityNotFoundException e) {
			return false;
		}
	}
	
}
