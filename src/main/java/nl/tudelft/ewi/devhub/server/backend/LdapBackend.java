package nl.tudelft.ewi.devhub.server.backend;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import org.apache.directory.api.ldap.codec.decorators.SearchResultEntryDecorator;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.api.ldap.model.message.BindResponse;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.Response;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;

/**
 * <p>
 * The {@link LdapBackend} class is responsible for authenticating users against a specified LDAP
 * server using the user's specified netID and password. DevHub is designed to run without an
 * application service account in LDAP. Instead it uses the user's LDAP connection to sychronize its
 * database with the students and employees on the LDAP server periodically.
 * </p>
 * <p>
 * In addition to the periodic synchronization, DevHub also pulls information from the LDAP server
 * when a user logs which is not yet present in the database.
 * </p>
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
@Slf4j
@Singleton
public class LdapBackend {

	private final Provider<Users> usersProvider;

	@Inject
	LdapBackend(Provider<Users> usersProvider) {
		this.usersProvider = usersProvider;
	}

	/**
	 * <p>
	 * This method will attempt to authenticate a user with the LDAP server. In case this is
	 * succesfull this method will return TRUE. If the user provided incorrect credentials or if the
	 * LDAP server could not be reached, FALSE will be returned.
	 * </p>
	 * <p>
	 * This method has one side-effect:
	 * <ul>
	 * <li>If the user succeeds in authenticating against the LDAP server but was not yet present in
	 * the database, this method will block until it has fetched his or her data from the LDAP
	 * server and persist it to the database.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param netId
	 *            The netID of the user attempting to log in.
	 * @param password
	 *            The password for the associated account.
	 * @return TRUE if LDAP succesfully authenticates these credentials, or FALSE otherwise.
	 */
	public boolean authenticate(String netId, String password) {
		log.debug("Authenticating: {}", netId);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(password));

		LdapConnection connection = null;
		try {
			connection = connect(netId, password);
			connection.close();

			ensureUserPresent(netId, password);

			return true;
		}
		catch (Throwable e) {
			log.debug(e.getMessage(), e);
			return false;
		}
	}

	private void ensureUserPresent(String netId, String password) throws LdapException, IOException {
		Users database = usersProvider.get();

		try {
			database.findByNetId(netId);
		}
		catch (EntityNotFoundException e) {
			log.trace("Persisting user: {} since he/she is not yet present in the database", netId);
			User user = fetch(netId, password);
			database.persist(user);
		}
	}

	private User fetch(String netId, String password) throws LdapException, IOException {
		log.debug("Fetching user from LDAP: {}", netId);

		try (LdapConnection conn = connect(netId, password)) {
			List<LdapEntry> results = search(netId, conn);
			if (results.isEmpty()) {
				throw new LdapException("Could not find user with netID: " + netId);
			}

			LdapEntry entry = results.get(0);

			User user = new User();
			user.setNetId(entry.getNetId());
			user.setName(entry.getName());
			user.setEmail(entry.getEmail());

			return user;
		}
	}

	private String getValue(Entry entry, String key) throws LdapInvalidAttributeValueException {
		Attribute value = entry.get(key);
		if (value == null) {
			return null;
		}
		return value.getString();
	}

	private LdapConnection connect(String netId, String password) throws LdapException, IOException {
		LdapConnection conn = new LdapNetworkConnection("ldaps.tudelft.nl", 636, true);
		BindRequest request = new BindRequestImpl();
		request.setSimple(true);
		request.setName(netId + "@tudelft.net");
		request.setCredentials(password);

		BindResponse response = conn.bind(request);
		LdapResult ldapResult = response.getLdapResult();
		ResultCodeEnum resultCode = ldapResult.getResultCode();
		if (resultCode != ResultCodeEnum.SUCCESS) {
			conn.close();
			return null;
		}

		return conn;
	}

	private List<LdapEntry> search(String netId, LdapConnection conn) throws LdapInvalidDnException, LdapException {
		SearchRequest searchRequest = new SearchRequestImpl();
		searchRequest.setBase(new Dn("OU=MDS,DC=tudelft,DC=net"));
		searchRequest.setScope(SearchScope.SUBTREE);
		searchRequest.setFilter("(uid=" + netId + ")");

		SearchCursor cursor = null;
		try {
			cursor = conn.search(searchRequest);
			List<LdapEntry> entries = Lists.newArrayList();
			Iterator<Response> iterator = cursor.iterator();
			while (iterator.hasNext()) {
				Response response = iterator.next();
				SearchResultEntryDecorator decorator = (SearchResultEntryDecorator) response;

				Entry entry = decorator.getEntry();
				String id = getValue(entry, "uid");
				String email = getValue(entry, "mail");
				String name = getValue(entry, "displayName");
				if (name.contains(" - ")) {
					name = name.substring(0, name.indexOf(" - "));
				}

				entries.add(new LdapEntry(name, id, email));
			}

			Collections.sort(entries, new Comparator<LdapEntry>() {
				@Override
				public int compare(LdapEntry o1, LdapEntry o2) {
					String netId1 = o1.getNetId();
					String netId2 = o2.getNetId();
					return netId1.compareTo(netId2);
				}
			});
			return entries;
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Data
	private static class LdapEntry {
		private final String name;
		private final String netId;
		private final String email;
	}

	public static interface LdapUserProcessor {
		void synchronize(String prefix, List<LdapEntry> entries);
	}

	public static class PersistingLdapUserProcessor implements LdapUserProcessor {

		private final Provider<UnitOfWork> workProvider;
		private final Provider<Users> users;

		@Inject
		PersistingLdapUserProcessor(Provider<UnitOfWork> workProvider, Provider<Users> users) {
			this.workProvider = workProvider;
			this.users = users;
		}

		@Override
		public void synchronize(String prefix, List<LdapEntry> entries) {
			UnitOfWork work = workProvider.get();
			try {
				work.begin();
				synchronizeInternally(prefix, entries);
			}
			catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
			finally {
				work.end();
			}
		}

		@Transactional
		protected void synchronizeInternally(String prefix, List<LdapEntry> entries) {
			Users database = users.get();
			List<User> currentUsers = database.listAllWithNetIdPrefix(prefix);

			while (!currentUsers.isEmpty() || !entries.isEmpty()) {
				int compare = compareFirstItems(currentUsers, entries);
				if (compare == 0) {
					currentUsers.remove(0);
					LdapEntry ldapEntry = entries.remove(0);
					log.trace("User: {} already present in both LDAP and database", ldapEntry.getNetId());
				}
				else if (compare < 0) {
					User current = currentUsers.remove(0);
					log.trace("Removing user: {} since he/she is no longer present in LDAP", current.getNetId());
					database.delete(current);
				}
				else if (compare > 0) {
					LdapEntry entry = entries.remove(0);

					User user = new User();
					user.setNetId(entry.getNetId());
					user.setName(entry.getName());
					user.setEmail(entry.getEmail());

					log.trace("Persisting user: {} since he/she is present in LDAP", user.getNetId());
					database.persist(user);
				}
			}
		}

		private int compareFirstItems(List<User> fromDatabase, List<LdapEntry> fromLdap) {
			if (fromDatabase.isEmpty() && fromLdap.isEmpty()) {
				throw new IndexOutOfBoundsException();
			}
			else if (fromDatabase.isEmpty() && !fromLdap.isEmpty()) {
				return 1;
			}
			else if (!fromDatabase.isEmpty() && fromLdap.isEmpty()) {
				return -1;
			}

			User current = fromDatabase.get(0);
			LdapEntry entry = fromLdap.get(0);

			String netId1 = current.getNetId();
			String netId2 = entry.getNetId();
			return netId1.compareTo(netId2);
		}

	}

}
