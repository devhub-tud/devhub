package nl.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import nl.devhub.server.database.entities.QUser;
import nl.devhub.server.database.entities.User;

public class Users extends Controller<User> {

	@Inject
	public Users(EntityManager entityManager) {
		super(entityManager);
	}
	
	public User find(long id) {
		return query().from(QUser.user)
				.where(QUser.user.id.eq(id))
				.singleResult(QUser.user);
	}
	
}
