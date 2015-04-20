package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.QCommit;

public class Commits extends Controller<Commit> {

	@Inject
	public Commits(final EntityManager entityManager) {
		super(entityManager);
	}
	
	@Transactional
	public Commit retrieve(Group group, String commitId) {
		return query().from(QCommit.commit)
			.where(QCommit.commit.repository.eq(group))
			.where(QCommit.commit.commitId.eq(commitId))
			.singleResult(QCommit.commit);
	}
	
	@Transactional
	public Commit ensureExists(Group group, String commitId) {
		Commit commit = retrieve(group, commitId);
		
		if(commit == null) {
			commit = new Commit();
			commit.setCommitId(commitId);
			commit.setRepository(group);
			commit.setComments(Lists.<CommitComment> newArrayList());
			commit = persist(commit);
		}
		
		return commit;
	}
	
}
