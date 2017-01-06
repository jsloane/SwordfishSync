package swordfishsync

import swordfishsync.exceptions.TorrentClientException

import grails.core.GrailsApplication
import grails.transaction.Transactional
import org.springframework.mail.MailSender

import static org.springframework.http.HttpStatus.*

@Transactional(readOnly = true)
class ConfigurationController {
	
    GrailsApplication grailsApplication
	MailSender mailSender
	TorrentClientService torrentClientService
	
	static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
	
    def index() {
		respond Configuration.findByTitle('Configuration'), model: [
			messages: Message.list()
		]
    }
	
	def edit() {
		respond Configuration.findByTitle('Configuration'), model: [
			messages: Message.list()
		]
	}
	
    @Transactional
    def save() {
		def errorMessages = []
		def settings = Setting.list()
		settings.each { setting ->
			boolean doSave = false
			if (params.containsKey(setting.code)) {
				def newValue = params.get(setting.code)
				if (newValue != setting.value) {
					setting.value = newValue
					doSave = true
				}
			} else if (setting.type == 'boolean') {
				setting.value = false
				doSave = true
			}
			if (doSave) {
				if (!setting.save()) {
					errorMessages.add('Error saving ' + Configuration.findBySetting(setting).title)
				} else {
					if (setting.code == 'email.host') {
						grailsApplication.config.grails.mail.host = setting.valueObject
						mailSender.setHost(grailsApplication.config.grails.mail.host)
					}
					if (setting.code == 'email.port') {
						grailsApplication.config.grails.mail.port = setting.valueObject
						mailSender.setPort(grailsApplication.config.grails.mail.port)
					}
					if (['torrent.type', 'torrent.host', 'torrent.port', 'torrent.username', 'torrent.password'].contains(setting.code)) {
						// torrent config changed
						torrentClientService.setTorrentClient()
					}
				}
			}
		}
		
		if (errorMessages) {
			flash.error = 'Error saving configuration'
			respond Configuration.findByTitle('Configuration'), view: 'edit', model: [
				messages: Message.list(),
				errorMessages: errorMessages
			]
		} else {
			flash.message = 'Configuration saved'
			respond Configuration.findByTitle('Configuration'), view: 'index', model: [
				messages: Message.list()
			]
		}
	}
	
    def show(Configuration configuration) {
        respond configuration, model: [
			messages: Message.list()
		]
    }

    def create() {
        respond new Configuration(params)
    }

    /*@Transactional
    def save(Configuration configuration) {
        if (configuration == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (configuration.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond configuration.errors, view:'create'
            return
        }

        configuration.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'configuration.label', default: 'Configuration'), configuration.id])
                redirect configuration
            }
            '*' { respond configuration, [status: CREATED] }
        }
    }*/

    /*def edit(Configuration configuration) {
        respond configuration
    }*/

    @Transactional
    def update(Configuration configuration) {
        if (configuration == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (configuration.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond configuration.errors, view:'edit'
            return
        }

        configuration.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'configuration.label', default: 'Configuration'), configuration.id])
                redirect configuration
            }
            '*'{ respond configuration, [status: OK] }
        }
    }
	
	@Transactional
	def delete(Configuration configuration) {

		if (configuration == null) {
			transactionStatus.setRollbackOnly()
			notFound()
			return
		}

		configuration.delete flush:true

		request.withFormat {
			form multipartForm {
				flash.message = message(code: 'default.deleted.message', args: [message(code: 'configuration.label', default: 'Configuration'), configuration.id])
				redirect action:"index", method:"GET"
			}
			'*'{ render status: NO_CONTENT }
		}
	}
	
    @Transactional
    def purgeInprogressTorrents() {
		
		flash.successMessages = []
		flash.warningMessages = []
		flash.errorMessages = []
		
		def torrentStatesInProgress = TorrentState.findAllByStatus(TorrentState.Status.IN_PROGRESS)
		
		torrentStatesInProgress.each { TorrentState torrentState ->
			try {
				TorrentDetails torrentDetails = torrentClientService.getTorrentDetails(torrentState.torrent, false)
				
				if (!torrentDetails || TorrentDetails.Status.UNKNOWN.equals(torrentDetails?.status)) {
					// torrent details not returned from torrent client
					torrentState.status = TorrentState.Status.SKIPPED
					torrentState.save()
					flash.successMessages.add('Purged torrent [' + torrentState.torrent.name + ']')
				}
			} catch (TorrentClientException e) {
				flash.errorMessages.add('Error retrieving torrent details for torrent [' + torrentState.torrent.name + ']. Error: ' + e.toString())
			}
		}
		
		if (!flash.successMessages && !flash.errorMessages) {
			flash.warningMessages.add('No torrents purged')
		}
		
        redirect(action: 'index')
    }
	
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'configuration.label', default: 'Configuration'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
