package swordfishsync

class IndexController {
	
    def index() {
		
		render view: 'index', model: [
			messages: Message.list(),
			downloadingTorrents: TorrentState.findAllByStatus(TorrentState.Status.IN_PROGRESS),
			notifiedTorrents: TorrentState.findAllByStatus(TorrentState.Status.NOTIFIED_NOT_ADDED),
			completedTorrents: TorrentState.findAllByStatus(TorrentState.Status.COMPLETED)
		]
	}
	
	def clientTorrents() {
		
		
		render view: 'clientTorrents', model: []
	}
	
}
