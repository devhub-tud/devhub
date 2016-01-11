package nl.tudelft.ewi.devhub.server.backend.mail;

import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;
import nl.tudelft.ewi.devhub.server.web.templating.TranslatorFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.net.URI;
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
        RepositoryEntity repository = pullRequest.getRepository();

        URI uri = URI.create(config.getHttpUrl())
                .resolve(repository.getURI())
                .resolve("pull/")
                .resolve(Long.toString(pullRequest.getIssueId()));

        String link = uri.toASCIIString();
        List<Locale> locales = Collections.list(request.getLocales());
        Translator translator = factory.create(locales);

        repository.getCollaborators()
                .stream()
                .filter(user -> !user.equals(currentUser))
                .forEach(addressee -> {
                    String userName = addressee.getName();
                    String subject = translator.translate(COMMENT_SUBJECT, repository.getTitle());
                    String content = translator.translate(COMMENT_CONTENT, userName, currentUser.getName(), repository.getTitle(), link);
                    backend.sendMail(new MailBackend.Mail(addressee.getEmail(), subject, content));
                });
    }

}
