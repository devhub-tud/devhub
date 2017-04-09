package nl.tudelft.ewi.devhub.server.backend;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Courses;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.UserModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.git.web.api.GroupApi;
import nl.tudelft.ewi.git.web.api.GroupsApi;

import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class CoursesBackend {

    @Inject
    private CourseEditions courseEditions;

    @Inject
    private Courses courses;

    @Inject
    private Users users;

    @Inject
    private GroupsApi groupsApi;

    @Inject
    @Named("current.user")
    private User currentUser;

    /**
     * Create a course.
     *
     * @param course
     * 		Course to create
     */
    public void createCourse(Course course) {
        Preconditions.checkNotNull(course);
        checkAdmin();

        courses.persist(course);
        log.info("{} created course: {}", currentUser, course);
    }

    /**
     * Create a course edition.
     * 
     * @param course
     * 		CourseEdition to create
     */
    public void createCourse(CourseEdition course) {
        Preconditions.checkNotNull(course);
        checkAdmin();

        Collection<IdentifiableModel> admins = getAdmins();

        GroupModel groupModel = new GroupModel();
        groupModel.setName(gitoliteAssistantGroupName(course));
        groupModel.setMembers(admins);
        groupsApi.create(groupModel);

        try {
            courseEditions.persist(course);
            log.info("{} created course: {}", currentUser, course);
        }
        catch (Exception e) {
            // On failure, delete the group from the Gitolite config
            groupsApi.getGroup(groupModel.getName()).deleteGroup();
            throw e;
        }
    }

    /**
     * Save changes of an edited course.
     * 
     * @param course
     * 		The course to save
     */
    public void mergeCourse(CourseEdition course) {
        Preconditions.checkNotNull(course);
        checkAdmin();
        courseEditions.merge(course);
        log.info("{} updated course: {}", currentUser, course);
    }
    
    /**
     * Set the CourseAssistants for a CourseEdition, and add them to the course group on the Git Server
     * to access the repositories.
     * 
     * @param course
     * 		The CourseEdition to update
     * @param newAssistants
     * 		List of users assisting this course
     */
    @Transactional
    public void setAssistants(CourseEdition course, Collection<? extends User> newAssistants) {
        Preconditions.checkNotNull(course);
        Preconditions.checkNotNull(newAssistants);
        checkAdmin();

        Set<User> assistants = course.getAssistants();

        Set<User> toBeAdded = Sets.newHashSet();
        Set<User> toBeRemoved = Sets.newHashSet();

        retain(assistants, newAssistants, toBeAdded, toBeRemoved);


        try {
            GroupApi group = getGitoliteGroup(course);
            // Apply changes only to Git config after DB merge
            toBeAdded.stream().map(this::retrieveUser).forEach(group::addNewMember);
            toBeRemoved.stream().map(this::retrieveUser).forEach(group::removeMember);
        }
        catch (NotFoundException e) {
            GroupModel group = new GroupModel();
            group.setName(gitoliteAssistantGroupName(course));
            group.setMembers(Stream.concat(assistants.stream(), users.listAdministrators().stream())
               .distinct()
               .map(this::retrieveUser)
               .collect(Collectors.toList()));
            groupsApi.create(group);
        }

        courseEditions.merge(course);
        log.info("{} set the assistants for {} to {}", currentUser, course, assistants);
    }

    private static <T> void retain(Collection<T> collection, Collection<? extends T> retain,
                                   Collection<? super T> added, Collection<? super T> removed) {
        Iterator<T> iterator = collection.iterator();
        while(iterator.hasNext()) {
            T next = iterator.next();
            if(!retain.contains(next)) {
                iterator.remove();
                removed.add(next);
            }
        }
        for(T t : retain) {
            if(collection.add(t)) {
                added.add(t);
            }
        }
    }
    
    private void checkAdmin() throws UnauthorizedException {
        if (!currentUser.isAdmin()) {
            throw new UnauthorizedException();
        }
    }
    
    private List<IdentifiableModel> getAdmins() {
		return users.listAdministrators().stream()
				.map(this::retrieveUser)
				.collect(Collectors.toList());
    }

    private UserModel retrieveUser(User user) {
        UserModel userModel = new UserModel();
        userModel.setName(user.getNetId());
        return userModel;
    }

    private String gitoliteAssistantGroupName(CourseEdition courseEdition) {
        return String.format("@%s-%s",
           courseEdition.getCourse().getCode(),
           courseEdition.getCode()).toLowerCase();
    }

    private GroupApi getGitoliteGroup(CourseEdition course) {
        String groupName = gitoliteAssistantGroupName(course);
        return groupsApi.getGroup(groupName);
    }

}
