package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.QBuildResult;

import com.google.inject.persist.Transactional;

@Slf4j
public class BuildResults extends Controller<BuildResult> {

	@Inject
	public BuildResults(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public BuildResult find(Group group, String commitId) {
		BuildResult result = query().from(QBuildResult.buildResult)
				.where(QBuildResult.buildResult.repository.groupId.eq(group.getGroupId()))
				.where(QBuildResult.buildResult.commitId.eq(commitId))
				.singleResult(QBuildResult.buildResult);
		
		if (result == null) {
			throw new EntityNotFoundException();
		}
		return result;
	}
	
	@Transactional
	public boolean exists(Group group, String commitId) {
		try {
			find(group, commitId);
			return true;
		}
		catch (EntityNotFoundException e) {
			return false;
		}
		catch (Throwable e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}
	
}
