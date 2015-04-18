package nl.tudelft.ewi.devhub.server.backend.mail;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;
import nl.tudelft.ewi.devhub.server.web.templating.TranslatorFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class PullRequestMailer {

    private static final String COMMENT_SUBJECT = "mail.pull-request.subject";
    private static final String COMMENT_CONTENT = "mail.pull-request.content";

    private final Config config;
    private final MailBackend backend;
    private final TranslatorFactory factory;
    private final HttpServletRequest request;
    private final User currentUser;

    @Inject
    public PullRequestMailer(
            Config config,
            MailBackend backend,
            TranslatorFactory factory,
            @Context HttpServletRequest request,
            @Named("current.user") User currentUser) {
        this.config = config;
        this.backend = backend;
        this.factory = factory;
        this.request = request;
        this.currentUser = currentUser;
    }

    /**
     * Send a notification for new messages
     * @param pullRequest PullRequest to send
     */
    public void sendReviewMail(PullRequest pullRequest) {
        Group group = pullRequest.getGroup();
        Course course = group.getCourse();


        String link = String.format("%s/courses/%s/groups/%d/pull/%d",
                config.getHttpUrl(), course.getCode(), group.getGroupNumber(), pullRequest.getIssueId());
        List<Locale> locales = Collections.list(request.getLocales());
        Translator translator = factory.create(locales);

        group.getMembers()
                .stream()
                .filter(user -> !user.equals(currentUser))
                .forEach(addressee -> {
                    String userName = addressee.getName();
                    String subject = translator.translate(COMMENT_SUBJECT, group.getGroupName());
                    String content = translator.translate(COMMENT_CONTENT, userName, currentUser.getName(), group.getGroupName(), link);
                    backend.sendMail(new MailBackend.Mail(addressee.getEmail(), subject, content));
                });
    }

}
