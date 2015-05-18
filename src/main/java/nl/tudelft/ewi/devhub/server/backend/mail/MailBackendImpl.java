package nl.tudelft.ewi.devhub.server.backend.mail;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.Config;

import com.google.common.base.Strings;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Slf4j
@Singleton
public class MailBackendImpl implements MailBackend {

	private static final int MAIL_QUEUE_SIZE = 1000;

	private final Config config;
	private final Queue<Mail> mailQueue;
	private final ExecutorService executor;
	private final AtomicBoolean sending;

	@Inject
	public MailBackendImpl(final Config config) {
		this.config = config;
		this.mailQueue = Queues.newArrayBlockingQueue(MAIL_QUEUE_SIZE);
		this.executor = Executors.newSingleThreadExecutor();
		this.sending = new AtomicBoolean(false);
	}

	@Override
	public void sendMail(Mail mail) {
		if (Strings.isNullOrEmpty(mail.getAddressee())) {
			log.warn("Not sending mail: {}, since addressee has no email address set", mail);
			return;
		}

		synchronized (mailQueue) {
			mailQueue.offer(mail);
			if (sending.compareAndSet(false, true)) {
				executor.submit(new Mailer());
			}
		}
	}

	@Override
	public int getQueueSize() {
		synchronized (mailQueue) {
			return mailQueue.size();
		}
	}

	private class Mailer implements Runnable {

		@Override
		public void run() {
			while (true) {
				Mail mail;
				synchronized (mailQueue) {
					mail = mailQueue.poll();
					if (mail == null) {
						sending.set(false);
						return;
					}
				}

				try {
					MimeMessage message = createMail(mail);

					try {
						sendMail(mail, message);
					}
					catch (MessagingException e) {
						log.error(e.getMessage(), e);
					}
				}
				catch (Throwable e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		private MimeMessage createMail(Mail mail) throws MessagingException, UnsupportedEncodingException {
			String user = config.getSmtpUser();
			String pass = config.getSmtpPass();
			String host = config.getSmtpHost();
			String origin = config.getSmtpOrigin();

			Properties properties = System.getProperties();
			properties.setProperty("mail.smtp.host", host);
			if (!Strings.isNullOrEmpty(user)) {
				properties.setProperty("mail.user", user);
				properties.setProperty("mail.password", pass);
			}

			Session session = Session.getDefaultInstance(properties);

			MimeMessage message = new MimeMessage(session);
			message.addFrom(new Address[]{new InternetAddress(origin)});
			message.setSubject(mail.getSubject());
			message.setText(mail.getContent());
			message.setRecipient(RecipientType.TO, new InternetAddress(mail.getAddressee()));

			return message;
		}

		private void sendMail(Mail mail, MimeMessage message) throws MessagingException {
			log.info("Sending mail: " + mail);
			Transport.send(message);
		}

	}

}
