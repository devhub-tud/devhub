package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import nl.tudelft.ewi.devhub.server.database.entities.QUser;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.inject.persist.Transactional;

public class Users extends Controller<User> {

	@Inject
	public Users(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public User find(long id) {
		User user = query().from(QUser.user)
				.where(QUser.user.id.eq(id))
				.singleResult(QUser.user);
		
		if (user == null) {
			throw new EntityNotFoundException();
		}
		return user;
	}

	@Transactional
	public User findByNetId(String netId) {
		User user = query().from(QUser.user)
				.where(QUser.user.netId.eq(netId))
				.singleResult(QUser.user);
		
		if (user == null) {
			throw new EntityNotFoundException();
		}
		return user;
	}
	
}
