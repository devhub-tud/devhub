package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.database.controllers.PrivateRepositories;
import nl.tudelft.ewi.devhub.server.database.entities.PrivateRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Path("projects")
@RequestScoped
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class PrivateRepositoryOverview extends Resource {

	@Inject private TemplateEngine templateEngine;
	@Inject private PrivateRepositories privateRepositories;
	@Inject @Named("current.user") private User currentUser;
	@Inject private RepositoriesApi repositories;
	@Context private HttpServletRequest request;

	protected Map<String, Object> getBaseParameters() {
		Map<String, Object> parameters = Maps.newLinkedHashMap();
		parameters.put("user", currentUser);
		return parameters;
	}

	@GET
	@Transactional
	public Response getPrivateRepositoryOverview() {
		PrivateRepository privateRepository = new PrivateRepository();
		privateRepository.setOwner(currentUser);
		privateRepository.setTitle("test");
		privateRepository.setRepositoryName(currentUser.getNetId() + "/" + "test");
		privateRepositories.persist(privateRepository);

		CreateRepositoryModel createRepositoryModel = new CreateRepositoryModel();
		createRepositoryModel.setName(privateRepository.getRepositoryName());
		createRepositoryModel.setPermissions(ImmutableMap.of(currentUser.getNetId(), Level.ADMIN));
		repositories.createRepository(createRepositoryModel);

		return redirect(privateRepository.getURI());
	}

}
