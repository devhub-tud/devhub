package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;

import javax.persistence.EntityManager;
import java.util.Date;

import static nl.tudelft.ewi.devhub.server.database.entities.QCommit.commit;

public class Commits extends Controller<Commit> {

	private final Repositories repositories;

	@Inject
	public Commits(final EntityManager entityManager, final Repositories repositories) {
		super(entityManager);
		this.repositories = repositories;
	}
	
	@Transactional
	public Commit retrieve(Group group, String commitId) {
		return query().from(commit)
			.where(commit.repository.eq(group))
			.where(commit.commitId.eq(commitId))
			.singleResult(commit);
	}
	
	@Transactional
	public Commit ensureExists(Group group, String commitId) {
		Commit commit = retrieve(group, commitId);
		
		if(commit == null) {
			commit = createCommit(group, commitId);
		}
		
		return commit;
	}

	protected Commit createCommit(Group group, String commitId) {
		final nl.tudelft.ewi.git.client.Commit gitCommit = retrieveCommit(group, commitId);
		final Commit commit = new Commit();
		commit.setCommitId(commitId);
		commit.setRepository(group);
		commit.setComments(Lists.<CommitComment> newArrayList());
		commit.setCommitTime(new Date(gitCommit.getTime() * 1000));
		commit.setPushTime(new Date());
		commit.setAuthor(gitCommit.getAuthor());
		commit.setMerge(gitCommit.getParents().length > 1);
		return persist(commit);
	}

	@SneakyThrows
	protected nl.tudelft.ewi.git.client.Commit retrieveCommit(Group group, String commitId) {
		Repository repository = repositories.retrieve(group.getRepositoryName());
		return repository.retrieveCommit(commitId);
	}

	/**
	 * Check if a commit exists
	 * @param group Group
	 * @param commitId Commit id
	 * @return boolean
	 */
	@Transactional
	public boolean exists(Group group, String commitId) {
		return query().from(commit)
			.where(commit.repository.eq(group))
			.where(commit.commitId.eq(commitId))
			.exists();
	}
	
}
