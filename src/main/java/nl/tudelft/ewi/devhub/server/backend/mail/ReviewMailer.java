package nl.tudelft.ewi.devhub.server.backend.mail;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;
import nl.tudelft.ewi.devhub.server.web.templating.TranslatorFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class ReviewMailer {

    private static final String COMMENT_SUBJECT = "mail.assignment-review.subject";
    private static final String COMMENT_CONTENT = "mail.assignment-review.content";

    private final Config config;
    private final MailBackend backend;
    private final TranslatorFactory factory;
    private final HttpServletRequest request;

    @Inject
    ReviewMailer(MailBackend backend,
                 TranslatorFactory factory,
                 Config config,
                 HttpServletRequest request) {
        this.backend = backend;
        this.factory = factory;
        this.config = config;
        this.request = request;
    }

    /**
     * Send a notification for new messages
     * @param delivery Delivery to send
     */
    public void sendReviewMail(Delivery delivery) {
        Assignment assignment = delivery.getAssignment();
        Group group = delivery.getGroup();
        CourseEdition course = group.getCourse();
        Delivery.Review review = delivery.getReview();

        Preconditions.checkNotNull(assignment);
        Preconditions.checkNotNull(delivery);
        Preconditions.checkNotNull(review);

        String link = String.format("%s/courses/%s/groups/%d/assignments",
                config.getHttpUrl(), course.getCode(), group.getGroupNumber());
        List<Locale> locales = Collections.list(request.getLocales());
        Translator translator = factory.create(locales);

        group.getMembers()
                .stream()
                .forEach(addressee -> {
                    String userName = addressee.getName();
                    String subject = translator.translate(COMMENT_SUBJECT, assignment.getName());
                    String content = translator.translate(COMMENT_CONTENT, userName, assignment.getName(), delivery.getState().toString(), link);
                    backend.sendMail(new MailBackend.Mail(addressee.getEmail(), subject, content));
                });
    }

}
