package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

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
	public Commit retrieve(RepositoryEntity repository, String commitId) {
		return query().from(commit)
			.where(commit.repository.eq(repository))
			.where(commit.commitId.eq(commitId))
			.singleResult(commit);
	}
	
	@Transactional
	public Commit ensureExists(RepositoryEntity repositoryEntity, String commitId) {
		Commit commit = retrieve(repositoryEntity, commitId);
		
		if(commit == null) {
			commit = createCommit(repositoryEntity, commitId);
		}
		
		return commit;
	}

	protected Commit createCommit(RepositoryEntity repositoryEntity, String commitId) {
		final nl.tudelft.ewi.git.client.Commit gitCommit = retrieveCommit(repositoryEntity, commitId);
		final Commit commit = new Commit();
		commit.setCommitId(commitId);
		commit.setRepository(repositoryEntity);
		commit.setComments(Lists.<CommitComment> newArrayList());
		commit.setCommitTime(new Date(gitCommit.getTime() * 1000));
		commit.setPushTime(new Date());
		commit.setAuthor(gitCommit.getAuthor());
		commit.setMerge(gitCommit.getParents().length > 1);
		return persist(commit);
	}

	@SneakyThrows
	protected nl.tudelft.ewi.git.client.Commit retrieveCommit(RepositoryEntity repositoryEntity, String commitId) {
		Repository repository = repositories.retrieve(repositoryEntity.getRepositoryName());
		return repository.retrieveCommit(commitId);
	}

	/**
	 * Check if a commit exists
	 * @param repository RepositoryEntity
	 * @param commitId Commit id
	 * @return boolean
	 */
	@Transactional
	public boolean exists(RepositoryEntity repository, String commitId) {
		return query().from(commit)
			.where(commit.repository.eq(repository))
			.where(commit.commitId.eq(commitId))
			.exists();
	}
	
}
