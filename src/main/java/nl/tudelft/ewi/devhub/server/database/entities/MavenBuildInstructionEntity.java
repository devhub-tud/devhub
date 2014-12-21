package nl.tudelft.ewi.devhub.server.database.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.build.jaxrs.models.MavenBuildInstruction;

@Data
@Entity
@EqualsAndHashCode(callSuper=true)
@DiscriminatorValue("java-maven")
public class MavenBuildInstructionEntity extends BuildInstructionEntity {
	
	@NotNull
	@Column(name = "command")
	private String command;
	
	@Column(name = "withDisplay")
	private boolean withDisplay;

	@Override
	public MavenBuildInstruction getBuildInstruction() {
		MavenBuildInstruction instruction = new MavenBuildInstruction();
		instruction.setPhases(new String[] { command });
		instruction.setWithDisplay(withDisplay);
		return instruction;
	}

}
