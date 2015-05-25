package nl.tudelft.ewi.devhub.server.database.entities.builds;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import nl.tudelft.ewi.build.jaxrs.models.BuildInstruction;

@Entity
@Inheritance
@Table(name="build_instructions")
@DiscriminatorColumn(name="instruction_type")
public abstract class BuildInstructionEntity {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long groupId;
	
	public abstract BuildInstruction getBuildInstruction();
	
}
