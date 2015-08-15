package nl.tudelft.ewi.devhub.server.database.entities.builds;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.build.jaxrs.models.BuildInstruction;
import nl.tudelft.ewi.build.jaxrs.models.BuildRequest;
import nl.tudelft.ewi.build.jaxrs.models.GitSource;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.git.client.Repository;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.net.URLEncoder;

@Data
@Slf4j
@Entity
@Inheritance
@Table(name="build_instructions")
@DiscriminatorColumn(name="instruction_type")
public abstract class BuildInstructionEntity {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull(message = "error.course-timeout")
	@Column(name = "build_timeout")
	private Integer buildTimeout;

	/**
	 * Create the BuildInstruction for a build.
	 * @param config {@link Config Configuration} to use
	 * @param commit {@link Commit} to build
	 * @return a BuildInstruction
	 */
	protected abstract BuildInstruction getBuildInstruction(Config config, Commit commit);

	/**
	 * Create a new {@link BuildRequest} for this {@code BuildInstructionEntity}.
	 * @param config {@link Config Configuration} to use. The config is used to determine
	 * 	the callback URL for the build response.
	 * @param commit {@link Commit} to build
	 * @param repository {@link Repository} to use. The repository is used to determine the
	 * 	clone URL for the repository.
	 * @return BuildRequest to be made
	 */
	public BuildRequest createBuildRequest(final Config config, final Commit commit, final Repository repository) {
		BuildRequest buildRequest = new BuildRequest();
		buildRequest.setInstruction(getBuildInstruction(config, commit));
		buildRequest.setSource(createGitSource(repository, commit));
		buildRequest.setTimeout(getBuildTimeout());
		buildRequest.setCallbackUrl(getCallbackUrl(config, commit, "build-result"));
		log.debug("Created build request {}", buildRequest);
		return buildRequest;
	}

	/**
	 * Get a hook callback URL.
	 * @param config {@link Config Configuration} to use
	 * @param commit {@link Commit}  to build
	 * @param resource Path to return to
	 * @return combined callback url
	 */
	@SneakyThrows
	protected String getCallbackUrl(final Config config, final Commit commit, final String resource) {
		StringBuilder callbackBuilder = new StringBuilder();
		callbackBuilder.append(config.getHttpUrl());
		callbackBuilder.append("/hooks/").append(resource);
		callbackBuilder.append("?repository=" + URLEncoder.encode(commit.getRepository().getRepositoryName(), "UTF-8"));
		callbackBuilder.append("&commit=" + URLEncoder.encode(commit.getCommitId(), "UTF-8"));
		return callbackBuilder.toString();
	}

	/**
	 * Create a GitSource for the group.
	 * @param repository Repository to use
	 * @param commit Commit to be build
	 * @return GitSource
	 */
	protected GitSource createGitSource(final Repository repository, final Commit commit) {
		GitSource gitSource = new GitSource();
		gitSource.setCommitId(commit.getCommitId());
		gitSource.setRepositoryUrl(repository.getUrl());
		log.debug("Created git source {}", gitSource);
		return gitSource;
	}
	
}
