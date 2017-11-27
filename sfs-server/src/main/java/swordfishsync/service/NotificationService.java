package swordfishsync.service;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.TemplateResolver;

import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Torrent;
import swordfishsync.exceptions.ApplicationException;
import swordfishsync.model.TorrentContent;
import swordfishsync.service.NotificationService.Type;

@Service("notificationService")
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${email.from}")
    String emailFrom;
    
	@Resource
    TemplateResolver emailTemplateResolver;

	@Resource
	SpringTemplateEngine templateEngine;
	
	@Resource
	JavaMailSender mailSender;
    
	public enum Type {
		AVAILABLE, COMPLETED
	}

	public void sendNotification(FeedProvider feedProvider, Torrent torrent, TorrentContent torrentContent, Type type) throws ApplicationException {

		if (StringUtils.isNotBlank(feedProvider.getNotifyEmail())) {
			// TODO String emailSubject = Setting.valueFor("app.title") + " Notification";
			String emailSubject = "SwordfishSync Notification";
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
				//ctx.setVariable("tmdbNotice", "tmdbNotice"); // TODO Setting.valueFor('media.tmdb.notice'),
				//ctx.setVariable("tvdbNotice", "tvdbNotice"); // TODO Setting.valueFor('media.tvdb.notice')

				String htmlContent = this.templateEngine.process("notify", ctx);
				
				log.info("Sending notification email to: " + feedProvider.getNotifyEmail());
				
				MimeMessage mimeMessage = this.mailSender.createMimeMessage();
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				try {
					message.setFrom(emailFrom); // TODO Setting.valueFor('email.from')
					message.setTo(feedProvider.getNotifyEmail());
					message.setSubject(emailSubject);
					message.setText(htmlContent, true);
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mailSender.send(mimeMessage);
			} catch (Exception e) {
				throw new ApplicationException("Error sending notification email", e);
			}
		}
	}
	
	
}
