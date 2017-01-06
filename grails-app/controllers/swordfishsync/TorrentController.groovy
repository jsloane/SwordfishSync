package swordfishsync

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import swordfishsync.exceptions.TorrentClientException

@Transactional(readOnly = true)
@Slf4j
class TorrentController {

	FeedService feedService
	TorrentClientService torrentClientService
	
    def index(Integer max) {
		def torrentList = Torrent.list(params)
		def feedProvider = null
		if (params.feedProviderId) {
			feedProvider = FeedProvider.get(params.feedProviderId)
			torrentList = feedProvider?.feed.torrents
		}
        respond ([torrentList: torrentList, feedProvider: feedProvider])
    }

    def show(Torrent torrent) {
        respond torrent
    }
	
	def download() {
		Torrent torrent = Torrent.get(params.id)
		FeedProvider feedProvider = FeedProvider.get(params.feedProviderId)
		
		TorrentState.Status torrentStatus = feedService.getTorrentStatus(feedProvider, torrent)
		
		if (feedProvider && torrentStatus != TorrentState.Status.IN_PROGRESS && torrentStatus != TorrentState.Status.COMPLETED && torrentStatus != TorrentState.Status.NOTIFY_COMPLETED) {
			try {
				torrentClientService.addTorrent(feedProvider, torrent)
				flash.successMessages = ['Downloading ' + torrent.name]
			} catch (TorrentClientException e) {
				log.error('Error adding torrent: ' + torrent.name, e)
				flash.errorMessages = ['Error adding torrent: ' + e.toString()]
			}
		} else {
			flash.errorMessages = ['Torrent is already downloading or completed']
		}
		
		
		redirect action: 'index', params: ['feedProviderId': feedProvider.id]
	}

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'torrent.label', default: 'Torrent'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
