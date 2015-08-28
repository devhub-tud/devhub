package nl.tudelft.ewi.devhub.server.backend;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;

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

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Singleton
public class LdapAuthenticationProvider implements AuthenticationProvider {
	
	private final Config config;
	private final Provider<Users> userProvider;
	private final BasicAuthenticationProvider basicAuthenticationProvider;
	
	@Inject
	public LdapAuthenticationProvider(Config config, Provider<Users> userProvider, BasicAuthenticationProvider basicAuthenticationProvider) {
		this.config = config;
		this.userProvider = userProvider;
		this.basicAuthenticationProvider = basicAuthenticationProvider;
	}

	@Override
	public AuthenticationSession authenticate(final String username, final String password)
			throws AuthenticationProviderUnavailable,
			InvalidCredentialsException {

		try {
			// Try to login from cache, on failure login using ldap
			return basicAuthenticationProvider.authenticate(username, password);
		}
		catch (InvalidCredentialsException e) {
			final LdapConnection connection = connect(username, password);

			return new AuthenticationSession() {

				@Override
				public void fetch(User user) throws IOException {
					try {
						List<LdapEntry> results = search(username, connection);

						if(!results.isEmpty()) {
							LdapEntry entry = results.get(0);
							user.setNetId(entry.getNetId());
							user.setName(entry.getName());
							user.setEmail(entry.getEmail());
							user.setPassword(password);
						}
					} catch (LdapException e) {
						throw new IOException(e);
					}
				}

				@Override
				public boolean synchronize(User user) throws IOException {
					if(!user.isPasswordMatch(password)) {
						user.setPassword(password);
						return true;
					}
					return false;
				}

				@Override
				public void close() throws IOException {
					connection.close();
				}

			};
		}
	}
	
	/**
	 * Try to establish a {@link LdapConnection}
	 * 
	 * @param netId
	 *            Username for the user
	 * @param password
	 *            Password
	 * @return {@link LdapConnection}
	 * @throws InvalidCredentialsException
	 *             If the supplied credentials aren't correct
	 * @throws AuthenticationProviderUnavailable
	 *             If an error occurred while connecting to the LDAP server
	 */
	private LdapConnection connect(String netId, String password)
			throws InvalidCredentialsException,
			AuthenticationProviderUnavailable {
		
		LdapConnection conn = null;
		
		try {
			conn = new LdapNetworkConnection(config.getLDAPHost(),
					config.getLDAPPort(), config.isLDAPSSL());
			BindRequest request = new BindRequestImpl();
			request.setSimple(true);
			request.setName(netId + config.getLDAPExtension());
			request.setCredentials(password);
	
			BindResponse response = conn.bind(request);
			LdapResult ldapResult = response.getLdapResult();
			
			ResultCodeEnum resultCode = ldapResult.getResultCode();
			
			switch(resultCode){ 
			case SUCCESS:
				return conn;
			case INVALID_CREDENTIALS:
				conn.close();
				throw new InvalidCredentialsException();
			default:
				conn.close();
				throw new AuthenticationProviderUnavailable(ldapResult.getDiagnosticMessage());
			}
		}
		catch (LdapException | IOException e) {
			
			if(conn != null && conn.isConnected()) {
				try {
					conn.close();
				} catch(IOException e1) {
					log.info(e.getMessage(), e1);
				}
			}
			
			throw new AuthenticationProviderUnavailable(e);
		}
	}
	
	private String getValue(Entry entry, String key) throws LdapInvalidAttributeValueException {
		Attribute value = entry.get(key);
		if (value == null) {
			return null;
		}
		return value.getString();
	}
	
	private List<LdapEntry> search(String netId, LdapConnection conn) throws LdapInvalidDnException, LdapException {
		SearchRequest searchRequest = new SearchRequestImpl();
		searchRequest.setBase(new Dn(config.getLDAPPrimaryDomain()));
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
