package swordfishsync

class IndexController {
	
	TorrentClientService torrentClientService
	
    def index() {
		render view: 'index', model: [
			messages: Message.list(),
			downloadingTorrents: TorrentState.findAllByStatus(TorrentState.Status.IN_PROGRESS),
			notifiedTorrents: TorrentState.findAllByStatus(TorrentState.Status.NOTIFIED_NOT_ADDED),
			completedTorrents: TorrentState.findAllByStatus(TorrentState.Status.COMPLETED) // todo: include NOTIFY_COMPLETED
		]
	}
	
	def clientTorrents() {
		def clientTorrents = torrentClientService.getAllTorrents()
		render view: 'clientTorrents', model: [clientTorrents: clientTorrents]
	}
	
}
