package nl.tudelft.ewi.devhub.server.database.controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.entities.QUser;
import nl.tudelft.ewi.devhub.server.database.entities.User;

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

		return ensureNotNull(user, "Could not find user with id: " + id);
	}

	@Transactional
	public User findByNetId(String netId) {
		Preconditions.checkNotNull(netId);
		
		User user = query().from(QUser.user)
			.where(QUser.user.netId.equalsIgnoreCase(netId))
			.singleResult(QUser.user);

		return ensureNotNull(user, "Could not find user with netID:" + netId);
	}

	@Transactional
	public List<User> listAllWithNetIdPrefix(String prefix) {
		Preconditions.checkNotNull(prefix);
		
		return query().from(QUser.user)
			.where(QUser.user.netId.startsWithIgnoreCase(prefix))
			.orderBy(QUser.user.netId.toLowerCase().asc())
			.list(QUser.user);
	}

	@Transactional
	public List<User> listAdministrators() {
		return query().from(QUser.user)
			.where(QUser.user.admin.isTrue())
			.list(QUser.user);
	}

	@Transactional
	public Map<String, User> mapByNetIds(Set<String> netIds) {
		Preconditions.checkNotNull(netIds);
		
		if (netIds.isEmpty()) {
			return Maps.newHashMap();
		}

		List<String> lowerCasedNetIds = netIds.stream().map(String::toLowerCase).collect(Collectors.toList());

		List<User> result = query().from(QUser.user)
			.where(QUser.user.netId.toLowerCase().in(lowerCasedNetIds))
			.orderBy(QUser.user.netId.toLowerCase().asc())
			.list(QUser.user);

		Map<String, User> mapping = Maps.newHashMap();
		for (User user : result) {
			mapping.put(user.getNetId(), user);
		}
		return mapping;
	}

}
