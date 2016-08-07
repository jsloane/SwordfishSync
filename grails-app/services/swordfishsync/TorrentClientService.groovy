package swordfishsync

import javax.annotation.PostConstruct
import swordfishsync.exceptions.TorrentClientException

import ca.benow.transmission.TransmissionClient
import grails.plugin.cache.GrailsCacheManager;
import grails.transaction.Transactional
import groovy.util.logging.Slf4j

@Transactional
@Slf4j
class TorrentClientService {
	
	GrailsCacheManager grailsCacheManager
	TorrentClient torrentClient
	FeedService feedService
	
	void init() {
		if (Setting.valueFor('torrent.type') == 'Transmission') {
			// transmission client
			
			torrentClient = null
			
			TransmissionClient transmissionClient = new TransmissionClient(
				Setting.valueFor('torrent.host'),
				Setting.valueFor('torrent.port'),
				Setting.valueFor('torrent.username'),
				Setting.valueFor('torrent.password')
			)
			
			if (transmissionClient.rpcVersion != 0) {
				torrentClient = new TransmissionTorrentClient(
					grailsCacheManager: grailsCacheManager,
					transmissionClient: transmissionClient,
					feedService: feedService
				)
			} else {
				String errorMessage = 'Error setting Torrent Client.'
				log.error(errorMessage)
				
				Message torrentClientError = Message.findWhere(
					type: Message.Type.DANGER,
					category: Message.Category.TORRENT_CLIENT
				)
				if (!torrentClientError) {
					torrentClientError = new Message(
						type: Message.Type.DANGER,
						category: Message.Category.TORRENT_CLIENT
					)
				}
				torrentClientError.dateCreated = new Date()
				torrentClientError.message = errorMessage
				torrentClientError.save()
			}
		}
	}
	
    def addTorrent(FeedProvider feedProvider, Torrent torrent) throws TorrentClientException {
		if (!torrentClient) {
			throw new TorrentClientException('Torrent Client not set')
		}
		
		log.info 'Adding torrent name[' + torrent.name + '], url[' + torrent.url + ']'
		
		try {
			torrentClient.addTorrent(feedProvider, torrent)
		} catch (IOException e) {
			throw new TorrentClientException('Error adding torrent', e)
		}
    }
	
    def moveTorrent(Torrent torrent, String directory) throws TorrentClientException {
		if (!torrentClient) {
			throw new TorrentClientException('Torrent Client not set')
		}
		
		try {
			torrentClient.moveTorrent(torrent, directory)
		} catch (IOException e) {
			throw new TorrentClientException('Error moving torrent', e)
		}
    }
	
    def removeTorrent(Torrent torrent, Boolean deleteData) throws TorrentClientException {
		if (!torrentClient) {
			throw new TorrentClientException('Torrent Client not set')
		}
		
		try {
			torrentClient.removeTorrent(torrent, deleteData)
		} catch (IOException e) {
			throw new TorrentClientException('Error removing torrent', e)
		}
    }
	
	TorrentDetails getTorrentDetails(Torrent torrent, Boolean includeFiles) {
		if (!torrentClient) {
			throw new TorrentClientException('Torrent Client not set')
		}
		
		TorrentDetails details = null
		try {
			details = torrentClient.getTorrentDetails(torrent, includeFiles)
		} catch (TorrentClientException | IOException e) {
			log.error('Error getting torrent status', e)
			// set error message field
		}
		return details
	}
	
}
