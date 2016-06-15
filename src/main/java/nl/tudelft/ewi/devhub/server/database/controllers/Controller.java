package nl.tudelft.ewi.devhub.server.database.controllers;

import com.mysema.commons.lang.CloseableIterator;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;
import com.mysema.query.jpa.impl.JPAQuery;

import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionImplementor;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
public class Controller<T> {

	protected final EntityManager entityManager;

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
	public <V extends T> V refresh(V entity) {
		entityManager.refresh(entity);
		return entity;
	}

	@Transactional
	public T delete(T entity) {
		entityManager.remove(entity);
		entityManager.flush();
		return entity;
	}

	public JPAQuery query() {
		return new JPAQuery(entityManager);
	}

	protected T ensureNotNull(T entry, String error) {
		if (entry == null) {
			throw new EntityNotFoundException(error);
		}
		return entry;
	}

	public <V> V unproxy(V object) {
		Hibernate.initialize(object);
		return (V) entityManager.unwrap(SessionImplementor.class)
			.getPersistenceContext()
			.unproxy(object);
	}

	protected <V> Stream<V> toStream(CloseableIterator<V> closeableIterator) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(closeableIterator, Spliterator.ORDERED), false)
			.onClose(closeableIterator::close);
	}

}
