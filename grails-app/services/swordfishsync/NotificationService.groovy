package swordfishsync

import grails.transaction.Transactional
import groovy.util.logging.Slf4j;
import swordfishsync.exceptions.ApplicationException

@Transactional
@Slf4j
class NotificationService {
	
	grails.gsp.PageRenderer groovyPageRenderer
	
	public enum Type {
		AVAILABLE, COMPLETED
	}
	
    def sendNotification(FeedProvider feedProvider, Torrent torrent, TorrentContent torrentContent, NotificationService.Type type) throws ApplicationException {
		if (feedProvider.notifyEmail) {
			String emailSubject = Setting.valueFor('app.title') + ' Notification'
			if (NotificationService.Type.AVAILABLE.equals(type)) {
				emailSubject = feedProvider.name + ' download available: ' + torrentContent.name
			} else if (NotificationService.Type.COMPLETED.equals(type)) {
				emailSubject = feedProvider.name + ' download complete: ' + torrentContent.name
			}
			
			try {
				def content = groovyPageRenderer.render(view: '/email/notify', model: [
					torrent: torrent,
					torrentContent: torrentContent,
					type: type,
					tmdbNotice: Setting.valueFor('media.tmdb.notice'),
					tvdbNotice: Setting.valueFor('media.tvdb.notice')
				])
				
				log.info('Sending notification email to: ' + feedProvider.notifyEmail)
				sendMail {
					to		feedProvider.notifyEmail
					from	Setting.valueFor('email.from')
					subject	emailSubject
					html	content
				}
			} catch (Exception e) {
				throw new ApplicationException('Error sending notification email', e)
			}
		}
    }
	
}
