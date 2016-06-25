package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.inject.persist.Transactional;
import nl.tudelft.ewi.devhub.server.database.controllers.CommitComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.IssueComments;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Event;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class CourseEventFeed {

    @Inject
    PullRequests pullRequests;

    @Inject
    Commits commits;

    @Inject
    Deliveries deliveries;

    @Inject
    IssueComments issueComments;

    @Inject
    CommitComments commitComments;

    @Inject
    Groups groups;

    @Transactional
    public List<? extends Event> getEventsFor(CourseEdition courseEdition, int limit) {
        List<Group> groups = courseEdition.getGroups();
        List<GroupRepository> groupRepositories = Lists.transform(groups, Group::getRepository);

        return CourseEventFeed.<Event>concatAll(
            pullRequests.findLastPullRequests(groupRepositories, limit),
            commits.getMostRecentCommits(groupRepositories, limit),
            commitComments.getMostRecentCommitComments(groupRepositories, limit),
            issueComments.getMostRecentIssueComments(groupRepositories, limit),
            deliveries.getMostRecentDeliveries(groups, limit)
        ).sorted(Ordering.natural().reversed()).limit(limit).collect(Collectors.toList());
    }

    public static <T> Stream<? extends T> concatAll(Stream<? extends T>... streams) {
        return Stream.of(streams).reduce(Stream::concat).get();
    }

}
