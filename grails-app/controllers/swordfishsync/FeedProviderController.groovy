package swordfishsync

import java.io.IOException
import java.util.ArrayList
import java.util.Date
import java.util.List

import org.apache.commons.lang3.StringUtils

import swordfishsync.exceptions.TorrentClientException

import ca.benow.transmission.model.AddedTorrentInfo;
import grails.transaction.Transactional
import groovy.util.logging.Slf4j

@Slf4j
@Transactional(readOnly = true)
class FeedProviderController {
	
	TorrentClientService torrentClientService
	
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(/*Integer max*/) {
        //params.max = Math.min(max ?: 10, 100)
        //respond FeedProvider.list(/*sort: 'name'*//*params*/)//, model: [
			//feedProviderList: FeedProvider.list(/*params*/)
			//,feedProviderCount: FeedProvider.count(),
			//,feed: Feed.get(1)
		//]
        respond FeedProvider.list(), model: [
			messages: Message.list()
		]
    }

    def show(FeedProvider feedProvider) {
        respond feedProvider, model: [
			messages: Message.list(),
			addFilterAttributes: feedProvider?.filterAttributes.findAll { FeedProvider.FeedFilterAction.ADD.equals(it.filterType) },
			ignoreFilterAttributes: feedProvider?.filterAttributes.findAll { FeedProvider.FeedFilterAction.IGNORE.equals(it.filterType) }
		]
    }

    def create() {
        respond new FeedProvider(params)
    }
	
	@Transactional
	def addTorrent(FeedProvider feedProvider) {
		if (feedProvider.id && params.torrentUrls) {
			flash.errorMessages = []
			flash.successMessages = []
			
			for (String torrentUrl : params.torrentUrls.split("\\n")) {
				torrentUrl = torrentUrl.trim();
				if (StringUtils.isNotBlank(torrentUrl)) {
					Torrent newTorrent = Torrent.findByUrl(torrentUrl)
					if (!newTorrent) {
						newTorrent = new Torrent(
							feed:					feedProvider.feed,
							inCurrentFeed:			false,
							addedToTorrentClient:	false,
							url:					torrentUrl
						).save()
					}
					
					if (newTorrent && newTorrent.addedToTorrentClient) {
						flash.successMessages.add('Torrent [' + newTorrent.name + '] already added')
					} else {
						try {
							torrentClientService.addTorrent(feedProvider, newTorrent)
							flash.successMessages.add('Added torrent [' + newTorrent.name + ']')
							newTorrent.save()
						} catch (TorrentClientException e) {
							log.error('Error adding manual torrent', e)
							newTorrent.discard()
							flash.errorMessages.add('Error adding torrent [' + torrentUrl + "]. Error: " + e.getMessage());
						}
					}
				}
			}
			
			if (flash.successMessages && !flash.errorMessages) {
				redirect feedProvider
			} else {
				respond feedProvider, model: [
					messages: Message.list()
				]
			}
		} else {
			respond feedProvider, model: [
				messages: Message.list()
			]
		}
	}

    @Transactional
    def save(FeedProvider feedProvider) {
        if (feedProvider == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }
		
		Feed feed = Feed.findByUrl(params.url)
		if (!feed) {
			feed = new Feed(url: params.url)
			feed.save()

	        if (feed.hasErrors()) {
	            transactionStatus.setRollbackOnly()
	            respond feed.errors, view:'create'
	            return
	        }
		}
		feedProvider.feed = feed
		feedProvider.save()
		
        if (feedProvider.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond feedProvider.errors, view:'create'
            return
        }

		feedProvider.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'feedProvider.label', default: 'FeedProvider'), feedProvider.id])
                redirect feedProvider
            }
            '*' { respond feedProvider, [status: CREATED] }
        }
    }

    def edit(FeedProvider feedProvider) {
        respond feedProvider, model: [
			messages: Message.list()
		]
    }

    @Transactional
    def update(FeedProvider feedProvider) {
		println '### SAVE ###'
		println 'params: ' + params
		println 'params.url: ' + params.url
		
        if (feedProvider == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (feedProvider.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond feedProvider.errors, view:'edit'
            return
        }
		
		if (!feedProvider.feed.url.equals(params.url)) {
			Feed feed = Feed.findByUrl(params.url)
			if (!feed) {
				feed = new Feed(url: params.url)
				feed.save()
			}
			feedProvider.feed = feed
		}
		
        feedProvider.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'feedProvider.label', default: 'FeedProvider'), feedProvider.id])
                redirect feedProvider
            }
            '*'{ respond feedProvider, [status: OK] }
        }
    }

    @Transactional
    def delete(FeedProvider feedProvider) {

        if (feedProvider == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        feedProvider.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'feedProvider.label', default: 'FeedProvider'), feedProvider.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'feedProvider.label', default: 'FeedProvider'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
