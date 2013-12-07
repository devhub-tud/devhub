package nl.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.google.inject.persist.Transactional;
import com.mysema.query.jpa.impl.JPAQuery;

public class Controller<T> {
	
	private final EntityManager entityManager;

	@Inject
	public Controller(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	@Transactional
	public T persist(T entity) {
		entityManager.persist(entity);
		entityManager.flush();
		return entity;
	}
	
	@Transactional
	public T delete(T entity) {
		entityManager.remove(entity);
		entityManager.detach(entity);
		return entity;
	}
	
	@Transactional
	public JPAQuery query() {
		return new JPAQuery(entityManager);
	}

}
