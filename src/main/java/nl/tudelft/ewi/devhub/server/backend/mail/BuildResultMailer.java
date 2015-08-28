package nl.tudelft.ewi.devhub.server.backend.mail;

import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;
import nl.tudelft.ewi.devhub.server.web.templating.TranslatorFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BuildResultMailer {

	private static final String FAILED_SUBJECT = "mail.build-result-failed.subject";
	private static final String FAILED_CONTENT = "mail.build-result-failed.content";
	private static final int DISPLAY_LOG_LINES = 100;

	private final MailBackend backend;
	private final TranslatorFactory factory;

	@Inject
	BuildResultMailer(MailBackend backend, TranslatorFactory factory) {
		this.backend = backend;
		this.factory = factory;
	}

	public void sendFailedBuildResult(List<Locale> locales, BuildResult buildResult) {
		Preconditions.checkNotNull(locales);
		Preconditions.checkNotNull(buildResult);
		
		Translator translator = factory.create(locales);

		RepositoryEntity repository = buildResult.getRepository();
		String groupName = repository.getTitle();
		String log = lastLogLines(buildResult);

		String commitId = buildResult.getCommit().getCommitId();
		commitId = commitId.substring(0, 10);

		for (User addressee : repository.getCollaborators()) {
			String userName = addressee.getName();
			Object[] parameters = new Object[] { userName, commitId, groupName, DISPLAY_LOG_LINES, log };

			String subject = translator.translate(FAILED_SUBJECT, groupName);
			String content = translator.translate(FAILED_CONTENT, parameters);
			backend.sendMail(new MailBackend.Mail(addressee.getEmail(), subject, content));
		}
	}

	private String lastLogLines(BuildResult buildResult) {
		String log = buildResult.getLog();
		String[] lines = log.split("\n");
		if (lines.length <= DISPLAY_LOG_LINES) {
			return log;
		}

		String[] subRange = Arrays.copyOfRange(lines, lines.length - DISPLAY_LOG_LINES, lines.length);
		return StringUtils.join(subRange, '\n');
	}
}
