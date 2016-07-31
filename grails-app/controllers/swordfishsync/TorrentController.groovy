package swordfishsync

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class TorrentController {

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
