package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.PrivateRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import javax.persistence.EntityManager;

import java.util.List;

import static nl.tudelft.ewi.devhub.server.database.entities.QPrivateRepository.privateRepository;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class PrivateRepositories extends Controller<PrivateRepository> {

	@Inject
	public PrivateRepositories(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public PrivateRepository find(String username, String repositoryTitle) {
		Preconditions.checkNotNull(username);
		Preconditions.checkNotNull(repositoryTitle);

		return ensureNotNull(query().from(privateRepository)
				.where(privateRepository.owner.netId.equalsIgnoreCase(username)
					.and(privateRepository.title.equalsIgnoreCase(repositoryTitle)))
				.singleResult(privateRepository),
			"Could not find repository " + username + "/" + repositoryTitle);
	}

	@Transactional
	public List<PrivateRepository> findPrivateRepositories(User user) {
		Preconditions.checkNotNull(user);
		return query().from(privateRepository)
			.where(privateRepository.owner.eq(user)
			.or(privateRepository.collaborators.contains(user)))
			.list(privateRepository);
	}

}
