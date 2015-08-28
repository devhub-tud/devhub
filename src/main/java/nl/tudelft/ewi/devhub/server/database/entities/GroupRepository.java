package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.Delegate;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.Base;
import nl.tudelft.ewi.devhub.server.database.entities.builds.BuildInstructionEntity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.Collection;
import java.util.Map;

/**
 * A {@code GroupRepository} is a {@link RepositoryEntity} connected to a {@link Group}.
 */
@Data
@Entity
@DiscriminatorValue("1")
@EqualsAndHashCode(callSuper = true)
public class GroupRepository extends RepositoryEntity {

    @OneToOne(mappedBy = "repository", optional = false)
	@Delegate(types = {Base.class})
    private Group group;

    @Override
    public Collection<User> getCollaborators() {
        return getGroup().getMembers();
    }

    @Override
    public BuildInstructionEntity getBuildInstruction() {
        BuildInstructionEntity buildInstructionEntity = super.getBuildInstruction();
        if(buildInstructionEntity == null) {
            buildInstructionEntity = getGroup().getCourseEdition().getBuildInstruction();
        }
        return buildInstructionEntity;
    }

	@Override
	public Map<String, String> getProperties() {
		return getCourseEdition().getProperties();
	}

	@Override
    public String getTitle() {
        return group.getGroupName();
    }

    protected CourseEdition getCourseEdition() {
        return getGroup().getCourseEdition();
    }

}
