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
	
	@PostConstruct
	void init() {
		// todo: if transmission set in settings, and get other config
		// try catch
		torrentClient = new TransmissionTorrentClient(
			grailsCacheManager: grailsCacheManager,
			transmissionClient: new TransmissionClient('192.168.1.100', 9091, 'transmission', 'transmission')
		)
		// todo: log error if unable to connect
	}
	
    def addTorrent(FeedProvider feedProvider, Torrent torrent) throws TorrentClientException {
		try {
			torrentClient.addTorrent(feedProvider, torrent)
		} catch (IOException e) {
			throw new TorrentClientException('Error adding torrent', e)
		}
    }
	
    def moveTorrent(Torrent torrent, String directory) throws TorrentClientException {
		try {
			torrentClient.moveTorrent(torrent, directory)
		} catch (IOException e) {
			throw new TorrentClientException('Error moving torrent', e)
		}
    }
	
    def removeTorrent(Torrent torrent, Boolean deleteData) throws TorrentClientException {
		try {
			torrentClient.removeTorrent(torrent, deleteData)
		} catch (IOException e) {
			throw new TorrentClientException('Error removing torrent', e)
		}
    }
	
	TorrentDetails getTorrentDetails(Torrent torrent, Boolean includeFiles) {
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
