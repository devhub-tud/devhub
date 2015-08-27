package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;
import com.mysema.query.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Controller<T> {

	private final EntityManager entityManager;

	@Inject
	public Controller(EntityManager entityManager) {
		Preconditions.checkNotNull(entityManager);
		this.entityManager = entityManager;
	}

	@Transactional
	public <V extends T> V persist(V entity) {
		entityManager.persist(entity);
		entityManager.flush();
		log.debug("Persisted {}", entity);
		return entity;
	}

	@Transactional
	public T merge(T entity) {
		entityManager.merge(entity);
		entityManager.flush();
		return entity;
	}

	@Transactional
	public T delete(T entity) {
		entityManager.remove(entity);
		entityManager.flush();
		return entity;
	}

	JPAQuery query() {
		return new JPAQuery(entityManager);
	}

	protected T ensureNotNull(T entry, String error) {
		if (entry == null) {
			throw new EntityNotFoundException(error);
		}
		return entry;
	}

}
