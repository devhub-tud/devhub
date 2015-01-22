package nl.tudelft.ewi.devhub.server.backend;

import lombok.Data;
import lombok.ToString;

import com.google.inject.ImplementedBy;

@ImplementedBy(MailBackendImpl.class)
public interface MailBackend {

	int getQueueSize();

	void sendMail(Mail mail);
	
	@Data
	@ToString(exclude = { "content" })
	public static class Mail {
		private final String addressee;
		private final String subject;
		private final String content;
	}

}
