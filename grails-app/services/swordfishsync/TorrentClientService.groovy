package swordfishsync

import java.util.List;

import javax.annotation.PostConstruct
import swordfishsync.exceptions.TorrentClientException

import ca.benow.transmission.TransmissionClient
import ca.benow.transmission.model.TorrentStatus;
import grails.plugin.cache.GrailsCacheManager;
import grails.transaction.Transactional
import groovy.util.logging.Slf4j

@Transactional
@Slf4j
class TorrentClientService {
	
	GrailsCacheManager grailsCacheManager
	TorrentClient torrentClient
	FeedService feedService
	
	/*void init() {
		setTorrentClient()
	}*/
	TorrentClient getTorrentClient() throws TorrentClientException {
		if (!torrentClient) {
			setTorrentClient()
		}
		if (!torrentClient) {
			 throw new TorrentClientException('Torrent client not available')
		}
		return torrentClient
	}
	
	void setTorrentClient() {
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
		//if (!getTorrentClient()) {
		//	throw new TorrentClientException('Torrent Client not set')
		//}
		
		log.info('Adding torrent [' + torrent.name + ']')
		
		try {
			// check that torrent is not already added to torrent client
			if (!torrent.addedToTorrentClient) {
				getTorrentClient().addTorrent(feedProvider, torrent)
				torrent.addedToTorrentClient = true
			}
			feedService.setTorrentStatus(feedProvider, torrent, TorrentState.Status.IN_PROGRESS)
		} catch (IOException e) {
			throw new TorrentClientException('Error adding torrent', e)
		}
    }
	
    def moveTorrent(Torrent torrent, String directory) throws TorrentClientException {
		//if (!torrentClient) {
		//	throw new TorrentClientException('Torrent Client not set')
		//}
		log.info('Moving torrent [' + torrent.name + '] to directory [' + directory + ']')
		
		try {
			getTorrentClient().moveTorrent(torrent, directory)
		} catch (IOException e) {
			throw new TorrentClientException('Error moving torrent', e)
		}
    }
	
    def removeTorrent(Torrent torrent, Boolean deleteData) throws TorrentClientException {
		//if (!torrentClient) {
		//	throw new TorrentClientException('Torrent Client not set')
		//}
		log.info('Removing torrent [' + torrent.name + ']')
		
		try {
			getTorrentClient().removeTorrent(torrent, deleteData)
		} catch (IOException e) {
			throw new TorrentClientException('Error removing torrent', e)
		}
    }
	
	TorrentDetails getTorrentDetails(Torrent torrent, Boolean includeFiles) throws TorrentClientException {
		//if (!torrentClient) {
		//	throw new TorrentClientException('Torrent Client not set')
		//}
		
		TorrentDetails details = null
		try {
			details = getTorrentClient().getTorrentDetails(torrent, includeFiles)
		} catch (IOException e) {
			throw new TorrentClientException('Error getting torrent details for torrent [' + torrent.name + ']', e)
		}
		return details
	}
	
	List<TorrentStatus> getAllTorrents() {
		List<TorrentStatus> allTorrentStatus = null
		try {
			allTorrentStatus = getTorrentClient().getAllTorrents()
		} catch (TorrentClientException | IOException e) {
			log.error('Error getting all torrent status', e)
			// todo: throw exception
		}
		return allTorrentStatus
	}
	
}
