package nl.tudelft.ewi.devhub.server.backend;

import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;

import nl.tudelft.ewi.git.web.api.KeysApi;
import nl.tudelft.ewi.git.web.api.UserApi;
import nl.tudelft.ewi.git.web.api.UsersApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SshKeyBackendTest extends BackendTest {

	@Mock KeysApi keysApi;
	@Mock UserApi userApi;
	@Mock UsersApi usersApi;
	@InjectMocks SshKeyBackend backend;
	
	private User user;
	private UserModel userModel;
	
	@Before
	public void beforeTest() {
		user = createUser();
		userModel = new UserModel();
		userModel.setName(user.getNetId());
		Mockito.when(usersApi.getUser(user.getNetId())).thenReturn(userApi);
		Mockito.when(userApi.get()).thenReturn(userModel);
		Mockito.when(userApi.keys()).thenReturn(keysApi);
	}
	
	@Test(expected=ApiError.class)
	public void testCreateInvalidKeyName() throws ApiError {
		backend.createNewSshKey(user, "keyna me", "ssh-rsa AAAA1242342 ");
	}
	
	@Test(expected=ApiError.class)
	public void testCreateInvalidKey() throws ApiError {
		backend.createNewSshKey(user, "keyname", "ss-rsa AAAA1242342 ");
	}
	
	@Test
	public void testCreate() throws ApiError {
		SshKeyModel model = new SshKeyModel();
		model.setContents("ssh-rsa AAAA1242342");
		model.setName("keyname");
		backend.createNewSshKey(user, model.getName(), model.getContents());
		Mockito.verify(keysApi).addNewKey(model);
	}
	
	@Test(expected=ApiError.class)
	public void testCreateDuplicateName() throws ApiError {
		SshKeyModel model = new SshKeyModel();
		model.setContents("ssh-rsa AAAA1242342");
		model.setName("keyname");
		backend.createNewSshKey(user, model.getName(), model.getContents());
		backend.createNewSshKey(user, model.getName(), model.getContents());
	}
	
	@Test(expected=ApiError.class)
	public void testCreateDuplicateKey() throws ApiError {
		SshKeyModel model = new SshKeyModel();
		model.setContents("ssh-rsa AAAA1242342");
		model.setName("keyname");
		backend.createNewSshKey(user, model.getName(), model.getContents());
		backend.createNewSshKey(user, model.getName().concat("A"), model.getContents());
	}
	
	@Test
	public void testDeleteSshKey() throws ApiError {
		SshKeyModel model = new SshKeyModel();
		model.setContents("ssh-rsa AAAA1242342");
		model.setName("keyname");
		backend.createNewSshKey(user, model.getName(), model.getContents());
		assertThat(userModel.getKeys(), contains(model));
		backend.deleteSshKey(user, model.getName());
		assertTrue(userModel.getKeys().isEmpty());
	}
	
	@Test
	public void testListEmptyKeys() throws ApiError {
		assertTrue(backend.listKeys(user).isEmpty());
		SshKeyModel model = new SshKeyModel();
		model.setContents("ssh-rsa AAAA1242342");
		model.setName("keyname");
		backend.createNewSshKey(user, model.getName(), model.getContents());
		assertThat(backend.listKeys(user), contains(model));
	}
	
	@Test(expected=ApiError.class)
	public void testDeleteNonExistingSshKey() throws ApiError {
		backend.deleteSshKey(user, "abcd");
	}
	
}
