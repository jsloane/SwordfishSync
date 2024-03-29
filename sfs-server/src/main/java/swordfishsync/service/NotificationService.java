package swordfishsync.service;

import java.util.Locale;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Message;
import swordfishsync.domain.Torrent;
import swordfishsync.exceptions.ApplicationException;
import swordfishsync.model.TorrentContent;

@Service("notificationService")
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	@Resource
	SettingService settingService;

	@Resource
	SpringTemplateEngine templateEngine;

	@Resource
	JavaMailSenderImpl mailSender;

	public enum Type {
		AVAILABLE, COMPLETED
	}

	public void sendNotification(FeedProvider feedProvider, Torrent torrent, TorrentContent torrentContent, Type type) throws ApplicationException {

		if (StringUtils.isNotBlank(feedProvider.getNotifyEmail())) {
			String emailSubject = settingService.getValue(SettingService.CODE_APP_NOTIFICATION_EMAIL_SUBJECT, String.class);
			if (NotificationService.Type.AVAILABLE.equals(type)) {
				emailSubject = feedProvider.getName() + " download available: " + torrentContent.getName();
			} else if (NotificationService.Type.COMPLETED.equals(type)) {
				emailSubject = feedProvider.getName() + " download complete: " + torrentContent.getName();
			}
			
			try {
				Context ctx = new Context(Locale.ENGLISH);
				ctx.setVariable("torrent", torrent);
				ctx.setVariable("torrentContent", torrentContent);
				ctx.setVariable("type", type);
				ctx.setVariable("tmdbNotice", settingService.getValue(SettingService.CODE_MEDIA_TMDB_NOTICE, String.class));
				ctx.setVariable("tvdbNotice", settingService.getValue(SettingService.CODE_MEDIA_TVDB_NOTICE, String.class));

				String htmlContent = this.templateEngine.process("notify", ctx);
				
				log.info("Sending notification email to: " + feedProvider.getNotifyEmail());
				
				MimeMessage mimeMessage = mailSender.createMimeMessage();
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				message.setFrom(settingService.getValue(SettingService.CODE_EMAIL_FROM, String.class));
				message.setTo(feedProvider.getNotifyEmail());
				message.setSubject(emailSubject);
				message.setText(htmlContent, true);
				mailSender.send(mimeMessage);
			} catch (Exception e) {
				throw new ApplicationException("Error sending notification email", e);
			}
		}

	}
	
	public void sendTestEmail(String email) throws ApplicationException {

		if (StringUtils.isNotBlank(email)) {
			String emailSubject = settingService.getValue(SettingService.CODE_APP_NOTIFICATION_EMAIL_SUBJECT, String.class) + ": Test email";
			
			try {
				Context ctx = new Context(Locale.ENGLISH);

				String htmlContent = this.templateEngine.process("test", ctx);
				
				log.info("Sending test email to: " + email);
				
				MimeMessage mimeMessage = mailSender.createMimeMessage();
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				message.setFrom(settingService.getValue(SettingService.CODE_EMAIL_FROM, String.class));
				message.setTo(email);
				message.setSubject(emailSubject);
				message.setText(htmlContent, true);
				mailSender.send(mimeMessage);
			} catch (Exception e) {
				throw new ApplicationException("Error sending test email", e);
			}
		}
		
	}
	
	public void sendMessageReport(Message message, Throwable throwable) throws ApplicationException {
		String reportEmail = settingService.getValue(SettingService.CODE_APP_ERROR_EMAIL, String.class);

		if (StringUtils.isNotBlank(reportEmail)) {
			String emailSubject = "SwordfishSync Error Report"; // TODO get from settings

			try {
				Context ctx = new Context(Locale.ENGLISH);
				ctx.setVariable("message", message);
				if (throwable != null) {
					ctx.setVariable("exceptionMessage", ExceptionUtils.getMessage(throwable));
					ctx.setVariable("exceptionStacktrace", ExceptionUtils.getStackTrace(throwable));
				}

				String htmlContent = this.templateEngine.process("error-report", ctx);
				
				log.info("Sending error report email to: " + reportEmail);
				
				MimeMessage mimeMessage = this.mailSender.createMimeMessage();
				MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				mimeMessageHelper.setFrom(settingService.getValue(SettingService.CODE_EMAIL_FROM, String.class));
				mimeMessageHelper.setTo(reportEmail);
				mimeMessageHelper.setSubject(emailSubject);
				mimeMessageHelper.setText(htmlContent, true);
				mailSender.send(mimeMessage);
			} catch (Exception e) {
				throw new ApplicationException("Error sending error report email", e);
			}
		}
	}
	
}
