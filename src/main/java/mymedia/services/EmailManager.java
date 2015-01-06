package mymedia.services;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import mymedia.db.form.TorrentInfo;
import mymedia.services.model.FeedProvider;
import mymedia.services.model.MediaInfo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

public class EmailManager {
	private MailSender mailSender;
    private SimpleMailMessage defaultTemplateMessage;
    private SimpleMailMessage newDefaultTemplateMessage;
    private SimpleMailMessage newTvTemplateMessage;
    private SimpleMailMessage newMovieTemplateMessage;
    private SimpleMailMessage completedDefaultTemplateMessage;
    private SimpleMailMessage completedTvTemplateMessage;
    private SimpleMailMessage completedMovieTemplateMessage;
    
    private static final String FEED_NAME = "{feedName}";
    private static final String TORRENT_NAME = "{torrentName}";
    private static final String MEDIA_NAME = "{mediaName}";
    private static final String POSTER_IMAGE = "{posterImage}";
    private static final String BACKDROP_IMAGE = "{backdropImage}";
    private static final String EXTRA_INFO = "{extraInfo}";
    private static final String DOWNLOAD_DIR = "{downloadDir}";
    private static final String SEASON_NUMBER = "{seasonNumber}";
    private static final String EPISODE_NUMBER = "{episodeNumber}";
    private static final String EPISODE_TITLE = "{episodeTitle}";
    private static final String EPISODE_DESCRIPTION = "{episodeDescription}";
    private static final String YEAR = "{year}";
    private static final String QUALITY = "{quality}";
    private static final String URL = "{url}";
    private static final String NOTICE = "{notice}";
    private String feedName;
    private String torrentName;
    private MediaInfo mediaInfo;
    private String url;
    private String posterUrl;
    private String backdropUrl;
    private String downloadDir;

    public MailSender getMailSender() {
        return mailSender;
    }

    public SimpleMailMessage getDefaultTemplateMessage() {
        return defaultTemplateMessage;
    }
    public SimpleMailMessage getNewDefaultTemplateMessage() {
        return newDefaultTemplateMessage;
    }
    public SimpleMailMessage getNewTvTemplateMessage() {
        return newTvTemplateMessage;
    }
    public SimpleMailMessage getNewMovieTemplateMessage() {
        return newMovieTemplateMessage;
    }
    public SimpleMailMessage getCompletedDefaultTemplateMessage() {
        return completedDefaultTemplateMessage;
    }
    public SimpleMailMessage getCompletedTvTemplateMessage() {
        return completedTvTemplateMessage;
    }
    public SimpleMailMessage getCompletedMovieTemplateMessage() {
        return completedMovieTemplateMessage;
    }
    public void setDefaultTemplateMessage(SimpleMailMessage defaultTemplateMessage) {
        this.defaultTemplateMessage = defaultTemplateMessage;
    }
    public void setNewDefaultTemplateMessage(SimpleMailMessage newDefaultTemplateMessage) {
        this.newDefaultTemplateMessage = newDefaultTemplateMessage;
    }
    public void setNewTvTemplateMessage(SimpleMailMessage newTvTemplateMessage) {
        this.newTvTemplateMessage = newTvTemplateMessage;
    }
    public void setNewMovieTemplateMessage(SimpleMailMessage newMovieTemplateMessage) {
        this.newMovieTemplateMessage = newMovieTemplateMessage;
    }
    public void setCompletedDefaultTemplateMessage(SimpleMailMessage completedDefaultTemplateMessage) {
        this.completedDefaultTemplateMessage = completedDefaultTemplateMessage;
    }
    public void setCompletedTvTemplateMessage(SimpleMailMessage completedTvTemplateMessage) {
        this.completedTvTemplateMessage = completedTvTemplateMessage;
    }
    public void setCompletedMovieTemplateMessage(SimpleMailMessage completedMovieTemplateMessage) {
        this.completedMovieTemplateMessage = completedMovieTemplateMessage;
    }

    public void setMailSender(MailSender mailSender){
        this.mailSender = mailSender;
    }

    public void sendMail(FeedProvider feedProvider, TorrentInfo torrent, MediaInfo mediaInfo, String messageType) throws MessagingException, MailException {
    	
    	String to = feedProvider.getFeedInfo().getNotifyEmail();
    	
    	feedName = feedProvider.getFeedInfo().getName();
    	torrentName = torrent.getName();
    	this.mediaInfo = mediaInfo;
    	posterUrl = mediaInfo.posterUrl;
    	backdropUrl = mediaInfo.backdropUrl;
    	

		if (feedProvider.getFeedInfo().getDownloadDirectory() != null && !feedProvider.getFeedInfo().getDownloadDirectory().isEmpty()) {
			downloadDir = MediaManager.constructDownloadDirectory(feedProvider, mediaInfo);
		}

    	url = "<a href=\"" + mediaInfo.url + "\">" + mediaInfo.url + "</a>";
    	if (posterUrl != null && !posterUrl.isEmpty()) {
    		posterUrl = "<img src=\"" + posterUrl + "\"/><br/><br/>";
    	}
    	if (backdropUrl != null && !backdropUrl.isEmpty()) {
    		backdropUrl = "<img src=\"" + backdropUrl + "\"/><br/><br/>";
    	}

    	SimpleMailMessage template = getMesageTemplate(messageType, mediaInfo.getType());
        JavaMailSenderImpl sender =(JavaMailSenderImpl) getMailSender();
        
        MimeMessage message = sender.createMimeMessage();
        SimpleMailMessage msg = new SimpleMailMessage(template);
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setFrom(msg.getFrom());
        helper.setSubject(parseStringValues(msg.getSubject()));
        helper.setText("<html>" + parseStringValues(msg.getText()) + "</html>", true);
        System.out.println("[DEBUG] EmailManager.sendMail sending email to: " + to + "...");
        try {
            sender.send(message);
        } catch (MailException ex) {
        	throw ex;
        }
    }
    
    private SimpleMailMessage getMesageTemplate(String messageType, String mediaType) {
    	SimpleMailMessage template = getDefaultTemplateMessage();
    	
    	if (messageType.equalsIgnoreCase("new")) {
        	template = getNewDefaultTemplateMessage();
        	if (mediaType.equals(MediaInfo.TYPE_TV)) {
        		template = getNewTvTemplateMessage();
        	} else if (mediaType.equals(MediaInfo.TYPE_MOVIE)) {
        		template = getNewMovieTemplateMessage();
        	}
    	} else if (messageType.equalsIgnoreCase("completed")) {
        	template = getCompletedDefaultTemplateMessage();
        	if (mediaType.equals(MediaInfo.TYPE_TV)) {
        		template = getCompletedTvTemplateMessage();
        	} else if (mediaType.equals(MediaInfo.TYPE_MOVIE)) {
        		template = getCompletedMovieTemplateMessage();
        	}
    	}
    	
		return template;
	}
    
	private String parseStringValues(String text) {
    	return text
			.replace(FEED_NAME, StringUtils.trimToEmpty(feedName))
    		.replace(TORRENT_NAME, StringUtils.trimToEmpty(torrentName))
    		.replace(MEDIA_NAME,  StringUtils.trimToEmpty(mediaInfo.getName()))
    		.replace(POSTER_IMAGE, StringUtils.trimToEmpty(posterUrl))
    		.replace(EXTRA_INFO, StringUtils.trimToEmpty(mediaInfo.extraInfo))
    		.replace(DOWNLOAD_DIR, StringUtils.trimToEmpty(downloadDir))
    		.replace(SEASON_NUMBER, StringUtils.trimToEmpty(mediaInfo.seasonNumber))
    		.replace(EPISODE_NUMBER, StringUtils.trimToEmpty(mediaInfo.episodeNumber))
    		.replace(EPISODE_TITLE, StringUtils.trimToEmpty(mediaInfo.episodeTitle))
    		.replace(EPISODE_DESCRIPTION, StringUtils.trimToEmpty(mediaInfo.episodeDescription))
    		.replace(BACKDROP_IMAGE, StringUtils.trimToEmpty(backdropUrl))
    		.replace(YEAR, StringUtils.trimToEmpty(mediaInfo.year))
    		.replace(QUALITY, StringUtils.trimToEmpty(mediaInfo.quality))
    		.replace(URL, StringUtils.trimToEmpty(url))
    		.replace(NOTICE, StringUtils.trimToEmpty(mediaInfo.notice))
		;
    }
}
