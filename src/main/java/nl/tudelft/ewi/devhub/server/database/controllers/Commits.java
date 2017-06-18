package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Commit.CommitId;
import nl.tudelft.ewi.devhub.server.database.entities.Event;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.events.CreateCommitEvent;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.tudelft.ewi.devhub.server.database.entities.QCommit.commit;

@Slf4j
public class Commits extends Controller<Commit> {

	private final AsyncEventBus eventBus;

	@Inject
	public Commits(final EntityManager entityManager, final AsyncEventBus eventBus) {
		super(entityManager);
		this.eventBus = eventBus;
	}
	
	@Transactional
	public Optional<Commit> retrieve(RepositoryEntity repository, String commitId) {
		CommitId key = new CommitId(repository.getId(), commitId);
		return Optional.ofNullable(entityManager.find(Commit.class, key));
	}

	@Transactional
	public List<Commit> retrieveCommits(RepositoryEntity repositoryEntity, Collection<String> commitIds) {
		return query().from(commit)
			.where(commit.repository.eq(repositoryEntity).and(commit.commitId.in(commitIds)))
			.list(commit);
	}

	/**
	 * Ensure that a commit exists in the database. Recursively check if the parents exists as well.
	 *
	 * @param repositoryEntity Repository to search commits for.
	 * @param commitId Commit id of the commit.
	 * @return The created commit entity.
	 */
	@Transactional
	public Commit ensureExists(RepositoryEntity repositoryEntity, String commitId) {
		return retrieve(repositoryEntity, commitId).orElseGet(() -> {
			final Commit commit = new Commit();
			commit.setCommitId(commitId);
			commit.setRepository(repositoryEntity);
			commit.setComments(Lists.newArrayList());
			commit.setPushTime(new Date());

			CreateCommitEvent createCommitEvent = new CreateCommitEvent();
			createCommitEvent.setCommitId(commitId);
			createCommitEvent.setRepositoryName(repositoryEntity.getRepositoryName());
			eventBus.post(createCommitEvent);
			return persist(commit);
		});
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

	/**
	 * List the most recent commits.
	 * @param repositoryEntities The repository entities to include.
	 * @param limit The maximal number of results.
     * @return A list of commits.
     */
	@Transactional
	public Stream<Commit> getMostRecentCommits(List<? extends RepositoryEntity> repositoryEntities, long limit) {
		return toStream(query().from(commit)
			.where(commit.repository.in(repositoryEntities))
			.orderBy(commit.pushTime.desc())
			.limit(limit)
			.iterate(commit));
	}
}
