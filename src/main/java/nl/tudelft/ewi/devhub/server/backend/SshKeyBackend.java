package nl.tudelft.ewi.devhub.server.backend;

import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.tudelft.ewi.git.web.api.UsersApi;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SshKeyBackend {

	private static final String COULD_NOT_CONNECT = "error.git-server-unavailable";
	private static final String DUPLICATE_KEY = "error.duplicate-key";
	private static final String INVALID_KEY_CONTENTS = "error.invalid-key-contents";
	private static final String INVALID_KEY_NAME = "error.invalid-key-name";
	private static final String NAME_ALREADY_EXISTS = "error.name-alread-exists";
	private static final String NO_SUCH_KEY = "error.no-such-key";
	
	private static final String REGEX_VALID_KEY_NAME = "^[a-zA-Z0-9]+$";

	private final UsersApi client;

	@Inject
	SshKeyBackend(UsersApi client) {
		this.client = client;
	}

	private void verifyKeyName(String keyName) throws ApiError {
		if (keyName == null || !keyName.matches(REGEX_VALID_KEY_NAME)) {
			throw new ApiError(INVALID_KEY_NAME);
		}
	}

	public void createNewSshKey(User user, String name, String contents) throws ApiError {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(contents);
		verifyKeyName(name);

		UserModel userModel = fetchUser(user.getNetId());
		for (SshKeyModel sshKeyModel : userModel.getKeys()) {
			if (sshKeyModel.getName()
				.equals(name)) {
				throw new ApiError(NAME_ALREADY_EXISTS);
			}
			if (sshKeyModel.getContents()
				.equals(contents.trim())) {
				throw new ApiError(DUPLICATE_KEY);
			}
		}

		SshKeyModel model = new SshKeyModel();
		model.setName(name);
		model.setContents(contents.trim());

		try {
			client.getUser(user.getNetId()).keys().addNewKey(model);
		}
		catch (BadRequestException | IllegalArgumentException e) {
			throw new ApiError(INVALID_KEY_NAME);
		}
	}

	public void deleteSshKey(User user, String name) throws ApiError  {
		Preconditions.checkNotNull(user);
		Preconditions.checkNotNull(name);

		try {
			client.getUser(user.getNetId()).keys().deleteSshKey(name);
		}
		catch (NotFoundException e) {
			throw new ApiError(NO_SUCH_KEY);
		}
	}

	public List<SshKeyModel> listKeys(User user) throws ApiError {
		Preconditions.checkNotNull(user);
		UserModel userModel = fetchUser(user.getNetId());
		List<SshKeyModel> keys = Lists.newArrayList(userModel.getKeys());
		Collections.sort(keys, new Comparator<SshKeyModel>() {
			@Override
			public int compare(SshKeyModel o1, SshKeyModel o2) {
				return o1.getName()
					.compareTo(o2.getName());
			}
		});

		return keys;
	}

	@Deprecated
	private UserModel fetchUser(String netId) throws ApiError {
		try {
			return client.getUser(netId).get();
		}
		catch (Throwable e) {
			throw new ApiError(COULD_NOT_CONNECT, e);
		}
	}

}
