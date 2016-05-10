package nl.tudelft.ewi.devhub.server.web.resources;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.database.entities.BuildServer;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;
import nl.tudelft.ewi.git.web.api.GroupApi;
import nl.tudelft.ewi.git.web.api.GroupsApi;
import nl.tudelft.ewi.git.web.api.KeysApi;
import nl.tudelft.ewi.git.web.api.UserApi;
import nl.tudelft.ewi.git.web.api.UsersApi;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildServerRegistrationTest {

	private static final String NOT_A_BUILDSERVER = "not-a-build-server";
	private static final String BUILD_SERVER_NAME = "build-server-1";
	private static final String BUILD_SERVER_HOST = "localhost";
	private static final String BUILD_SERVER_SECRET = "top-secret";

	@Spy JSch jSch = new JSch();
	@Mock UsersApi usersApi;
	@Mock UserApi userApi;
	@Mock KeysApi keysApi;
	@Mock GroupsApi groupsApi;
	@Mock GroupApi groupApi;
	@Mock BuildsBackend buildsBackend;
	@InjectMocks BuildServerRegistrationResource buildServerRegistrationResource;

	KeyPair valid;
	KeyPair invalid;
	GroupModel groupModel;

	@Before
	public void prepareBuildServerKey() throws JSchException {
		valid = KeyPair.genKeyPair(jSch, KeyPair.RSA);
		invalid = KeyPair.genKeyPair(jSch, KeyPair.RSA);

		SshKeyModel sshKeyModel = new SshKeyModel();
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		valid.writePublicKey(bas, BUILD_SERVER_NAME);
		sshKeyModel.setName(BUILD_SERVER_NAME);
		sshKeyModel.setContents(bas.toString());

		UserModel userModel = new UserModel();
		userModel.setName(BUILD_SERVER_NAME);
		userModel.setKeys(Collections.singleton(sshKeyModel));

		groupModel = new GroupModel();
		groupModel.setName(BuildServerRegistrationResource.BUILD_SERVERS_GROUP);
		groupModel.setMembers(Collections.singleton(userModel));

		when(usersApi.getUser(BUILD_SERVER_NAME)).thenReturn(userApi);
		when(userApi.get()).thenReturn(userModel);
		when(userApi.keys()).thenReturn(keysApi);

		when(groupsApi.getGroup(BuildServerRegistrationResource.BUILD_SERVERS_GROUP)).thenReturn(groupApi);
		when(groupApi.getGroup()).thenReturn(groupModel);
		when(groupApi.listMembers()).thenReturn(groupModel.getMembers());

		when(keysApi.listSshKeys()).thenReturn(Collections.singleton(sshKeyModel));
	}

	@Test
	public void testRegisterBuildServer() throws Exception {
		BuildServer buildServer = new BuildServer();
		buildServer.setHost(BUILD_SERVER_HOST);
		buildServer.setName(BUILD_SERVER_NAME);
		buildServer.setSecret(BUILD_SERVER_SECRET);

		String signature = getSignature(valid, BUILD_SERVER_SECRET);
		buildServerRegistrationResource.registerBuildServer(buildServer, signature);
	}

	@Test(expected = NotAuthorizedException.class)
	public void testRegisterBuildServerWithInvalidSignature() throws Exception {
		BuildServer buildServer = new BuildServer();
		buildServer.setHost(BUILD_SERVER_HOST);
		buildServer.setName(BUILD_SERVER_NAME);
		buildServer.setSecret(BUILD_SERVER_SECRET);

		String signature = getSignature(invalid, BUILD_SERVER_SECRET);
		buildServerRegistrationResource.registerBuildServer(buildServer, signature);
	}

	@Test(expected = NotAuthorizedException.class)
	public void testNotRegisterBuildServer() throws Exception {
		UserModel userModel = new UserModel();
		userModel.setName(NOT_A_BUILDSERVER);

		UserApi userApi = mock(UserApi.class);
		when(usersApi.getUser(NOT_A_BUILDSERVER)).thenReturn(userApi);
		when(userApi.get()).thenReturn(userModel);

		BuildServer buildServer = new BuildServer();
		buildServer.setHost(BUILD_SERVER_HOST);
		buildServer.setName(NOT_A_BUILDSERVER);
		buildServer.setSecret(BUILD_SERVER_SECRET);

		String signatue = getSignature(valid, BUILD_SERVER_SECRET);
		buildServerRegistrationResource.registerBuildServer(buildServer, signatue);
	}

	@Test(expected = InternalServerErrorException.class)
	public void internalServerErrorOnGroupNotFound() throws Exception {
		reset(groupsApi);
		when(groupsApi.getGroup(BuildServerRegistrationResource.BUILD_SERVERS_GROUP))
			.thenThrow(new NotFoundException("Build server not found"));

		testRegisterBuildServer();
	}

	private static String getSignature(KeyPair keyPair, String secret) {
		byte[] signature = keyPair.getSignature(secret.getBytes());
		return Base64.encodeBase64String(signature);
	}

}
