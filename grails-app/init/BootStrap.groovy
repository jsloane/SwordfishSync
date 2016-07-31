import grails.core.GrailsApplication
import org.springframework.mail.MailSender
import swordfishsync.Configuration
import swordfishsync.Setting

class BootStrap {
	
	GrailsApplication grailsApplication
	MailSender mailSender
	
    def init = { servletContext ->
		
		///////////////////////
		// Initilise settings
		///////////////////////
		
		def mainConfig = findOrCreateConfiguration(null, 'Configuration')
		def applicationConfig = findOrCreateConfiguration(mainConfig, 'Application')
		findOrCreateSetting(applicationConfig, 'Title', 'app.title', 'string', 'SwordfishSync')
		findOrCreateSetting(applicationConfig, 'Sync Interval (minutes)', 'app.syncInterval', 'integer', '5')
		def securityConfig = findOrCreateConfiguration(applicationConfig, 'Security')
		findOrCreateSetting(securityConfig, 'Enable Basic Authentication', 'app.security.enableBasicAuth', 'boolean', 'true')
		findOrCreateSetting(securityConfig, 'Username', 'app.security.username', 'string', 'swordfishsync')
		findOrCreateSetting(securityConfig, 'Password', 'app.security.password', 'string', 'swordfishsync')
		findOrCreateSetting(securityConfig, 'Accept untrusted SSL/TLS certificates', 'app.security.acceptUntrustedCertificates', 'boolean', 'false')
		def mediaConfig = findOrCreateConfiguration(mainConfig, 'Media')
		def tvdbConfig = findOrCreateConfiguration(mediaConfig, 'TVDB')
		findOrCreateSetting(tvdbConfig, 'API Key', 'media.tvdb.apikey', 'string', 'C43C7180A2A8E155') // todo - remove
		findOrCreateSetting(tvdbConfig, 'Notice', 'media.tvdb.notice', 'string', 'thetvdb.com')
		def tmdbConfig = findOrCreateConfiguration(mediaConfig, 'TMDb')
		findOrCreateSetting(tmdbConfig, 'API Key', 'media.tmdb.apikey', 'string', '5a1a77e2eba8984804586122754f969f') // todo - remove
		findOrCreateSetting(tmdbConfig, 'Notice', 'media.tmdb.notice', 'string', 'This product uses the TMDb API but is not endorsed or certified by TMDb.')
		def emailConfig = findOrCreateConfiguration(mainConfig, 'Email') // have to store this in grails config?
		findOrCreateSetting(emailConfig, 'Hostname', 'email.host', 'string', '192.168.1.100')
		findOrCreateSetting(emailConfig, 'Port', 'email.port', 'integer', '25')
		findOrCreateSetting(emailConfig, 'From address', 'email.from', 'string', 'SwordfishSync@sector101')
		def torrentHostConfig = findOrCreateConfiguration(mainConfig, 'Torrent Server')
		findOrCreateSetting(torrentHostConfig, 'Type', 'torrent.type', 'string', 'Transmission')
		findOrCreateSetting(torrentHostConfig, 'Hostname', 'torrent.host', 'string', 'localhost')
		findOrCreateSetting(torrentHostConfig, 'Port', 'torrent.port', 'integer', '25')
		findOrCreateSetting(torrentHostConfig, 'Username', 'torrent.username', 'string', '')
		findOrCreateSetting(torrentHostConfig, 'Password', 'torrent.password', 'string', '')
		//findOrCreateSetting(emailConfig, 'Username', 'torrent.username', 'string', '')
		//findOrCreateSetting(emailConfig, 'Password', 'torrent.password', 'string', '')
		//def databaseConfig = findOrCreateConfiguration(mainConfig, 'Database')
		
		// set mail config
		grailsApplication.config.grails.mail.host = Setting.valueFor('email.host')
		grailsApplication.config.grails.mail.port = Setting.valueFor('email.port')
		mailSender.setHost(grailsApplication.config.grails.mail.host)
		mailSender.setPort(grailsApplication.config.grails.mail.port)
		// todo: when updating this setting, update mailSender too
		
		
		
		// test feed
		if (!swordfishsync.Feed.findByUrl('https://kat.cr/tv/?rss=1')) {
			swordfishsync.Feed katTest = new swordfishsync.Feed(url: 'https://kat.cr/tv/?rss=1').save(failOnError: true)
			swordfishsync.FeedProvider katProviderTest = new swordfishsync.FeedProvider(
				feed: katTest,
				name: 'KAT TV',
				active: true,
				determineSubDirectory: true,
				//notifyEmail: 'james@sloane.id.au',
				notifyEmail: 'jisloane@hotmail.com',
				action: swordfishsync.FeedProvider.FeedAction.SKIP
			).save(failOnError: true)
		}
		// test messages
		//SUCCESS, INFO, WARNING, DANGER
		/*swordfishsync.Message.findByMessage('s message test') ?: new swordfishsync.Message(
			message: 's message test',
			type: swordfishsync.Message.Type.SUCCESS
		).save(failOnError: true)
		swordfishsync.Message.findByMessage('w message test') ?: new swordfishsync.Message(
			message: 'e message test',
			type: swordfishsync.Message.Type.WARNING
		).save(failOnError: true)
		swordfishsync.Message.findByMessage('w2 message test') ?: new swordfishsync.Message(
			message: 'e message test',
			type: swordfishsync.Message.Type.WARNING
		).save(failOnError: true)
		swordfishsync.Message.findByMessage('i message test') ?: new swordfishsync.Message(
			message: 'i message test',
			type: swordfishsync.Message.Type.INFO
		).save(failOnError: true)
		swordfishsync.Message.findByMessage('i2 message test') ?: new swordfishsync.Message(
			message: 'i message test',
			type: swordfishsync.Message.Type.INFO
		).save(failOnError: true)
		swordfishsync.Message.findByMessage('d message test') ?: new swordfishsync.Message(
			message: 'd message test',
			type: swordfishsync.Message.Type.DANGER
		).save(failOnError: true)*/
		
    }
    def destroy = {
    }
	
	def private findOrCreateConfiguration(Configuration parentConfig, String title) {
		if (parentConfig) {
			def configuration = parentConfig.childConfiguration.find { it.title == title }
			if (!configuration) {
				configuration = new Configuration(title: title)
				parentConfig.addToChildConfiguration(configuration)
			}
			return configuration
		} else {
			return Configuration.findByTitle(title) ?: new Configuration(
				title: title
			).save(failOnError: true)
		}
	}
	
	def private findOrCreateSetting(Configuration parentConfig, String title, String code, String type, String defaultValue) {
		def config = findOrCreateConfiguration(parentConfig, title)
		if (!config.setting) {
			config.setting = Setting.findByCode(code) ?: new Setting(
				code: code,
				type: type,
				value: defaultValue
			).save(failOnError: true)
		}
		return config.setting
	}
	
}
