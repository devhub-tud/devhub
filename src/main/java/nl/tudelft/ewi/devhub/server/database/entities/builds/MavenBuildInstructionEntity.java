package nl.tudelft.ewi.devhub.server.database.entities.builds;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.build.jaxrs.models.BuildInstruction;
import nl.tudelft.ewi.build.jaxrs.models.MavenBuildInstruction;
import nl.tudelft.ewi.build.jaxrs.models.plugins.MavenBuildPlugin;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * {@link BuildInstructionEntity} for Maven builds. Generates
 * {@link nl.tudelft.ewi.build.jaxrs.models.BuildRequest BuildRequests} with
 * {@link MavenBuildInstruction MavenBuildInstructions}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Slf4j
@Entity
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper=true)
@DiscriminatorValue("java-maven")
public class MavenBuildInstructionEntity extends BuildInstructionEntity {
	
	@NotNull
	@Column(name = "command")
	private String command;
	
	@Column(name = "with_display")
	private boolean withDisplay;

	@Column(name = "checkstyle")
	private boolean checkstyle;

	@Column(name = "findbugs")
	private boolean findbugs;

	@Column(name = "pmd")
	private boolean pmd;

	@Override
	protected BuildInstruction getBuildInstruction(final Config config, final Commit commit) {
		MavenBuildInstruction instruction = new MavenBuildInstruction();
		instruction.setPhases(command.split(" "));
		instruction.setWithDisplay(withDisplay);
		instruction.setPlugins(getBuildPlugins(config, commit, instruction));
		return instruction;
	}

	protected List<MavenBuildPlugin> getBuildPlugins(final Config config, final Commit commit, final MavenBuildInstruction instruction) {
		List<MavenBuildPlugin> plugins = Lists.newArrayList();
		if(isCheckstyle()) {
			plugins.add(getCheckstylePlugin(config, commit));
			log.debug("Adding Checkstyle to the build instruction {}", instruction);
		}
		if(isPmd()) {
			plugins.add(getPMDPlugin(config, commit));
			log.debug("Adding PMD to the build instruction {}", instruction);
		}
		if(isFindbugs()) {
			plugins.add(getFindBugsPlugin(config, commit));
			log.debug("Adding Findbugs to the build instruction {}", instruction);
		}
		return plugins;
	}

	protected MavenBuildPlugin getCheckstylePlugin(final Config config, final Commit commit) {
		MavenBuildPlugin checkstyle = new MavenBuildPlugin();
		checkstyle.setPhases(new String[]{"checkstyle:checkstyle"});
		checkstyle.setCallbackUrl(getCallbackUrl(config, commit, "checkstyle-result"));
		checkstyle.setFilePath("target/checkstyle-result.xml");
		checkstyle.setContentType(MediaType.APPLICATION_XML);
		return checkstyle;
	}

	protected MavenBuildPlugin getFindBugsPlugin(final Config config, final Commit commit) {
		MavenBuildPlugin findBugs = new MavenBuildPlugin();
		findBugs.setPhases(new String[]{"findbugs:findbugs"});
		findBugs.setCallbackUrl(getCallbackUrl(config, commit, "findbugs-result"));
		findBugs.setFilePath("target/findbugsXml.xml");
		findBugs.setContentType(MediaType.APPLICATION_XML);
		return findBugs;
	}

	protected MavenBuildPlugin getPMDPlugin(final Config config, final Commit commit) {
		MavenBuildPlugin pmd = new MavenBuildPlugin();
		pmd.setPhases(new String[]{"pmd:pmd"});
		pmd.setCallbackUrl(getCallbackUrl(config, commit, "pmd-result"));
		pmd.setFilePath("target/pmd.xml");
		pmd.setContentType(MediaType.APPLICATION_XML);
		return pmd;
	}

}
