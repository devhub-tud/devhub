package nl.tudelft.ewi.devhub.server.database.entities.builds;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.build.jaxrs.models.MavenBuildInstruction;
import nl.tudelft.ewi.devhub.server.database.entities.builds.BuildInstructionEntity;

@Data
@Entity
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
	public MavenBuildInstruction getBuildInstruction() {
		MavenBuildInstruction instruction = new MavenBuildInstruction();
		instruction.setPhases(command.split(" "));
		instruction.setWithDisplay(withDisplay);
		return instruction;
	}

}
