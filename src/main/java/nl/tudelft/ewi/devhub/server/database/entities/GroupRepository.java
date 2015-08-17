package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.entities.builds.BuildInstructionEntity;

import javax.persistence.*;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Jan-Willem on 8/11/2015.
 */
@Data
@Entity
@DiscriminatorValue("1")
@EqualsAndHashCode(callSuper = true)
public class GroupRepository extends RepositoryEntity {

    @OneToOne(mappedBy = "repository")
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
    public String getTitle() {
        return group.getGroupName();
    }

    protected CourseEdition getCourseEdition() {
        return getGroup().getCourseEdition();
    }

    @Override
    public URI getDevHubURI() {
        return URI.create("courses")
            .resolve(getCourseEdition().getCode())
            .resolve("group")
            .resolve(Long.toString(getGroup().getGroupNumber()));
    }

    @Override
    public Map<String, String> getProperties() {
        return getCourseEdition().getProperties();
    }

}
