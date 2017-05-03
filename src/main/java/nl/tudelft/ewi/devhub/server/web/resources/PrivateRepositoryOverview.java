package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.database.controllers.PrivateRepositories;
import nl.tudelft.ewi.devhub.server.database.entities.PrivateRepository;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.builds.MavenBuildInstructionEntity;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import org.hibernate.validator.constraints.NotEmpty;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
	public Response getPrivateRepositoryOverview() throws IOException {
		Map<String, Object> parameters = getBaseParameters();
		parameters.put("repositories", privateRepositories.findPrivateRepositories(currentUser));

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("projects.ftl", locales, parameters));
	}

	@POST
	@Path("setup")
	@Transactional
	public Response setupPrivateRepository(@FormParam("repositoryName") @NotEmpty @Pattern(regexp = "^\\w[\\w._@/+-]*[\\w._@+-]$") String repositoryName) {
		PrivateRepository privateRepository = new PrivateRepository();
		privateRepository.setTitle(repositoryName);
		privateRepository.setOwner(currentUser);
		privateRepository.setCollaborators(Lists.newArrayList(currentUser));
		privateRepository.setRepositoryName(currentUser.getNetId() + "/" + repositoryName);

		MavenBuildInstructionEntity buildInstruction = new MavenBuildInstructionEntity();
		buildInstruction.setCommand("test");
		buildInstruction.setWithDisplay(true);
		buildInstruction.setBuildTimeout(600000);
		privateRepository.setBuildInstruction(buildInstruction);

		privateRepositories.persist(privateRepository);

		CreateRepositoryModel createRepositoryModel = new CreateRepositoryModel();
		createRepositoryModel.setName(privateRepository.getRepositoryName());
		createRepositoryModel.setPermissions(ImmutableMap.of(currentUser.getNetId(), Level.ADMIN));
		repositories.createRepository(createRepositoryModel);

		return redirect(privateRepository.getURI());
	}

}
