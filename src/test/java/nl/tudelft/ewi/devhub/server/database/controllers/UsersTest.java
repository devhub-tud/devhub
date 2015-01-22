package nl.tudelft.ewi.devhub.server.database.controllers;

import java.math.BigInteger;
import java.util.Random;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import nl.tudelft.ewi.devhub.server.database.entities.User;
import static org.junit.Assert.*;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JukitoRunner.class)
@UseModules(TestDatabaseModule.class)
public class UsersTest {

	@Inject
	private Users users;
	
	@Inject
	private Random random;
	
	@Test(expected=ConstraintViolationException.class)
	public void testCreateUserWithoutNetId() {
		User user = new User();
		users.persist(user);
	}
	
	@Test
	public void testPersistUser() {
		User user = createUser();
		users.persist(user);
	}
	
	@Test(expected=PersistenceException.class)
	public void testPersistUserWithExistingId(){
		User user = createUser();
		users.persist(user);
		User other = new User();
		other.setId(user.getId());
		users.persist(other);
	}
	
	@Test(expected=PersistenceException.class)
	public void testPersistUserWithExistingNetId(){
		User user = createUser();
		users.persist(user);
		User other = new User();
		other.setNetId(user.getNetId());
		users.persist(other);
	}
	
	@Test
	public void testFetchUserById() {
		User user = createUser();
		users.persist(user);
		assertEquals(user, users.find(user.getId()));
	}
	
	@Test
	public void testFetchUserByNetId() {
		User user = createUser();
		users.persist(user);
		assertEquals(user, users.findByNetId(user.getNetId()));
	}
	
	protected User createUser() {
		User user = new User();
		user.setNetId(randomString());
		return user;
	}
	
	protected String randomString() {
		return new BigInteger(130, random).toString(32);
	}
	
}
