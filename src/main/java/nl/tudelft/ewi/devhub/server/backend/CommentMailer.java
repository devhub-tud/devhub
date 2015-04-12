package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.devhub.server.backend.MailBackend.Mail;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;
import nl.tudelft.ewi.devhub.server.web.templating.TranslatorFactory;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CommentMailer {

	private static final String COMMENT_SUBJECT = "mail.comment-in-project.subject";
	private static final String COMMENT_CONTENT = "mail.comment-in-project.content";

	private final Config config;
	private final MailBackend backend;
	private final TranslatorFactory factory;

	@Inject
	CommentMailer(MailBackend backend, TranslatorFactory factory, Config config) {
		this.backend = backend;
		this.factory = factory;
		this.config = config;
	}

	public void sendCommentMail(List<Locale> locales, Group group, CommitComment comment, String redirect) {
		Preconditions.checkNotNull(locales);
		
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
				backend.sendMail(new Mail(addressee.getEmail(), subject, content));
			});
	}

}
