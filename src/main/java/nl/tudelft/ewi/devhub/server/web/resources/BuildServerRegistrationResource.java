package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Signature;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.database.entities.BuildServer;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;
import nl.tudelft.ewi.git.web.api.GroupsApi;
import nl.tudelft.ewi.git.web.api.KeysApi;
import nl.tudelft.ewi.git.web.api.UserApi;
import nl.tudelft.ewi.git.web.api.UsersApi;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;
import org.jboss.resteasy.util.Base64;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.function.Predicate;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RequestScoped
@Path("build-servers")
@ValidateRequest
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class BuildServerRegistrationResource extends Resource {

	public static final String BUILD_SERVERS_GROUP = "@build-servers";
	private final JSch jSch;
	private final BuildsBackend backend;
	private final UsersApi usersApi;
	private final GroupsApi groupsApi;

	@Inject
	public BuildServerRegistrationResource(BuildsBackend backend, GroupsApi groupsApi, UsersApi usersApi, JSch jSch) {
		this.backend = backend;
		this.usersApi = usersApi;
		this.groupsApi = groupsApi;
		this.jSch = jSch;
	}

	/**
	 * Hook for automatically registering {@link BuildServer Build servers}.
	 * The Build Server may send this request to register itself as build server.
	 *
	 * It should sign the request using its private key, which is validated against
	 * the public key under the build server name in the git server. This ensures
	 * that the build server key has read access to the repositories and that the
	 * build server is allowed to join the pool.
	 *
	 * @param buildServer Build Server object, containing name, secret and hostname.
	 * @param signatureString The signature of the secret.
	 *
	 * @throws IOException If an I/O error occurs.
	 * @throws ApiError If an API error occurs.
	 * @throws InternalServerErrorException If the build-server group could not be found on the git server.
	 * @throws NotAuthorizedException If the build server is not authorized.
	 */
	@POST
	@Path("register")
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	public void registerBuildServer(@Valid BuildServer buildServer, @HeaderParam("Signature") String signatureString) throws IOException, ApiError, InternalServerErrorException, NotAuthorizedException {
		byte[] signature = Base64.decode(signatureString);

		class SignatureVerifier implements Predicate<Signature> {
			@Override
			@SneakyThrows
			public boolean test(Signature verifier) {
				verifier.update(buildServer.getSecret().getBytes());
				return verifier.verify(signature);
			}
		}

		checkBuildServerMembership(buildServer);
		KeysApi keysApi = usersApi.getUser(buildServer.getName()).keys();

		if (!keysApi.listSshKeys().stream()
			.map(this::getKeyPair)
			.map(KeyPair::getVerifier)
			.anyMatch(new SignatureVerifier())) {
			throw new NotAuthorizedException("Cannot verify signature of build server: " + buildServer.getName());
		}

		backend.addBuildServer(buildServer);
	}

	private void checkBuildServerMembership(BuildServer buildServer) {
		UserApi userApi = usersApi.getUser(buildServer.getName());
		UserModel userModel = userApi.get();

		try {
			if (!groupsApi.getGroup(BUILD_SERVERS_GROUP).listMembers().contains(userModel)) {
				throw new NotAuthorizedException(String.format("Build server %s is not a member of the %s group", buildServer.getName(), BUILD_SERVERS_GROUP));
			}
		}
		catch (NotFoundException e) {
			throw new InternalServerErrorException("Build-servers group could not be found: " + e.getMessage(), e);
		}
	}

	@SneakyThrows
	private KeyPair getKeyPair(SshKeyModel sshKeyModel) {
		return KeyPair.load(jSch, null, sshKeyModel.getContents().getBytes());
	}

}
