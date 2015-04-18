package nl.tudelft.ewi.devhub.server.backend.mail;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.database.entities.Comment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;
import nl.tudelft.ewi.devhub.server.web.templating.TranslatorFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * The comment mailer sends a mail notification for new comments
 * @author Jan-Willem Gmelig Meyling
 */
public class CommentMailer {

	private static final String COMMENT_SUBJECT = "mail.comment-in-project.subject";
	private static final String COMMENT_CONTENT = "mail.comment-in-project.content";

	private final Config config;
	private final MailBackend backend;
	private final TranslatorFactory factory;
	private final HttpServletRequest request;
	private final Group group;

	@Inject
	CommentMailer(MailBackend backend,
				  TranslatorFactory factory,
				  Config config,
				  @Named("current.group") Group group,
				  HttpServletRequest request) {
		this.backend = backend;
		this.factory = factory;
		this.config = config;
		this.group = group;
		this.request = request;

	}

	/**
	 * Send a notification for new messages
	 * @param comment Comment to send
	 * @param redirect URL at which the comment can be viewed
	 */
	public void sendCommentMail(Comment comment, String redirect) {
		Preconditions.checkNotNull(comment);
		Preconditions.checkNotNull(redirect);

		List<Locale> locales = Collections.list(request.getLocales());
		Translator translator = factory.create(locales);

		User commenter = comment.getUser();
		String commenterName = commenter.getName();
		String message = comment.getContent();
		String url = config.getHttpUrl().concat(redirect);
		String groupName = group.getGroupName();

		group.getMembers()
			.stream()
			.filter(addressee -> !addressee.equals(commenter))
			.forEach(addressee -> {
				String userName = addressee.getName();
				Object[] parameters = new Object[]{userName, commenterName, groupName, message, url};
				String subject = translator.translate(COMMENT_SUBJECT, groupName);
				String content = translator.translate(COMMENT_CONTENT, parameters);
				backend.sendMail(new MailBackend.Mail(addressee.getEmail(), subject, content));
			});
	}

}
