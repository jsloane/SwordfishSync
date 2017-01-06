package swordfishsync

import grails.core.GrailsApplication
import org.springframework.mail.MailSender
import swordfishsync.Configuration
import swordfishsync.Setting
import swordfishsync.TorrentClientService

class BootStrap {
	
	GrailsApplication grailsApplication
	MailSender mailSender
	TorrentClientService torrentClientService
	def grailsCacheManager
	
    def init = { servletContext ->
		
		//grailsCacheManager.getCache('torrentClient').nativeCache.cacheConfiguration.timeToLiveSeconds = 60
		
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
		findOrCreateSetting(tvdbConfig, 'API Key', 'media.tvdb.apikey', 'string', '')
		findOrCreateSetting(tvdbConfig, 'Notice', 'media.tvdb.notice', 'string', 'thetvdb.com')
		def tmdbConfig = findOrCreateConfiguration(mediaConfig, 'TMDb')
		findOrCreateSetting(tmdbConfig, 'API Key', 'media.tmdb.apikey', 'string', '')
		findOrCreateSetting(tmdbConfig, 'Notice', 'media.tmdb.notice', 'string', 'This product uses the TMDb API but is not endorsed or certified by TMDb.')
		def emailConfig = findOrCreateConfiguration(mainConfig, 'Email')
		findOrCreateSetting(emailConfig, 'Hostname', 'email.host', 'string', 'localhost')
		findOrCreateSetting(emailConfig, 'Port', 'email.port', 'integer', '25')
		findOrCreateSetting(emailConfig, 'From address', 'email.from', 'string', 'localhost')
		def torrentHostConfig = findOrCreateConfiguration(mainConfig, 'Torrent Server')
		findOrCreateSetting(torrentHostConfig, 'Type', 'torrent.type', 'string', 'Transmission')
		findOrCreateSetting(torrentHostConfig, 'Hostname', 'torrent.host', 'string', 'localhost')
		findOrCreateSetting(torrentHostConfig, 'Port', 'torrent.port', 'integer', '9091')
		findOrCreateSetting(torrentHostConfig, 'Username', 'torrent.username', 'string', 'transmission')
		findOrCreateSetting(torrentHostConfig, 'Password', 'torrent.password', 'string', 'transmission')
		
		// set mail config
		grailsApplication.config.grails.mail.host = Setting.valueFor('email.host')
		grailsApplication.config.grails.mail.port = Setting.valueFor('email.port')
		mailSender.setHost(grailsApplication.config.grails.mail.host)
		mailSender.setPort(grailsApplication.config.grails.mail.port)
		
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
