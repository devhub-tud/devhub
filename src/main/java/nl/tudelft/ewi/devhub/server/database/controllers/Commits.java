package nl.tudelft.ewi.devhub.server.database.controllers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import javax.persistence.EntityManager;
import java.util.Date;

import static nl.tudelft.ewi.devhub.server.database.entities.QCommit.commit;

@Slf4j
public class Commits extends Controller<Commit> {

	private final RepositoriesApi repositories;

	@Inject
	public Commits(final EntityManager entityManager, final RepositoriesApi repositories) {
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
		final Commit commit = new Commit();
		commit.setCommitId(commitId);
		commit.setRepository(repositoryEntity);
		commit.setComments(Lists.<CommitComment> newArrayList());
		commit.setPushTime(new Date());

		try {
			final CommitModel gitCommit = retrieveCommit(repositoryEntity, commitId);
			commit.setCommitTime(new Date(gitCommit.getTime() * 1000));
			commit.setAuthor(gitCommit.getAuthor());
			commit.setMerge(gitCommit.getParents().length > 1);
		}
		catch (Exception e) {
			log.warn("Failed to retrieve commit details", e);
		}

		return persist(commit);
	}

	@SneakyThrows
	protected CommitModel retrieveCommit(RepositoryEntity repositoryEntity, String commitId) {
		return repositories.getRepository(repositoryEntity.getRepositoryName())
			.getCommit(commitId)
			.get();
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
