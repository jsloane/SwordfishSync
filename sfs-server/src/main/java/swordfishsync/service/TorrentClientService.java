package swordfishsync.service;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ca.benow.transmission.TransmissionClient;
import ca.benow.transmission.model.TorrentStatus;
import swordfishsync.torrentclient.TorrentClient;
import swordfishsync.torrentclient.impl.TransmissionTorrentClient;
import swordfishsync.domain.Torrent;
//import swordfishsync.TorrentDetails;
import swordfishsync.domain.TorrentState;
import swordfishsync.exceptions.TorrentClientException;
import swordfishsync.model.TorrentDetails;
import swordfishsync.repository.TorrentStateRepository;

@Service("torrentClientService")
public class TorrentClientService {

    private static final Logger log = LoggerFactory.getLogger(TorrentClientService.class);
    
	TorrentClient torrentClient;

    @Value("${torrentclient.type}")
    String torrentClientType;
    
    @Value("${torrentclient.host}")
    String torrentClientHost;
    
    @Value("${torrentclient.port}")
    Integer torrentClientPort;
    
    @Value("${torrentclient.username}")
    String torrentClientUsername;
    
    @Value("${torrentclient.password}")
    String torrentClientPassword;
    
	@Resource
	SyncService syncService;

	@Resource
	TorrentStateRepository torrentStateRepository;
	
	TorrentClient getTorrentClient() throws TorrentClientException {
		if (torrentClient == null) {
			setTorrentClient();
		}
		if (torrentClient == null) {
			 throw new TorrentClientException("Torrent client not available");
		}
		return torrentClient;
	}
	
	private void setTorrentClient() {
		// todo:
		if ("Transmission".equals(torrentClientType)) {
			// transmission client
			
			torrentClient = null;
			
			TransmissionClient transmissionClient = new TransmissionClient(
				/*Setting.valueFor("torrent.host"),
				Setting.valueFor("torrent.port"),
				Setting.valueFor("torrent.username"),
				Setting.valueFor("torrent.password")*/
				torrentClientHost, torrentClientPort, torrentClientUsername, torrentClientPassword
			);
			
			if (transmissionClient.getRpcVersion() > 0) {
				torrentClient = new TransmissionTorrentClient(transmissionClient);
			} else {
				System.out.println("transmissionClient.getRpcVersion(): " + transmissionClient.getRpcVersion());

				// todo
				/*String errorMessage = 'Error setting Torrent Client.'
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
				torrentClientError.save()*/
			}
		} else {
			// throw exception (torrent client not supported)
		}
		
	}

	public boolean addTorrent(TorrentState torrentState) throws TorrentClientException {
		log.info("Adding torrent [" + torrentState.getTorrent().getName() + "]");
		
		boolean added = false;
		
		// check that torrent is not already added to torrent client
		if (!torrentState.getTorrent().getAddedToTorrentClient()) {
			getTorrentClient().addTorrent(torrentState);
			torrentState.getTorrent().setAddedToTorrentClient(true);
			syncService.setTorrentStatus(torrentState.getFeedProvider(), torrentState.getTorrent(), TorrentState.Status.IN_PROGRESS);
			torrentStateRepository.save(torrentState);
			added = true;
		}
		
		return added;
	}

	
	public void moveTorrent(Torrent torrent, String directory) throws TorrentClientException {
		log.info("Moving torrent [" + torrent.getName() + "] to directory [" + directory + "]");
		getTorrentClient().moveTorrent(torrent, directory);
    }
	
	public void removeTorrent(Torrent torrent, Boolean deleteData) throws TorrentClientException {
		log.info("Removing torrent [" + torrent.getName() + "]");
		getTorrentClient().removeTorrent(torrent, deleteData);
    }
	
	public TorrentDetails getTorrentDetails(Torrent torrent, Boolean includeFiles) throws TorrentClientException {
		return getTorrentClient().getTorrentDetails(torrent, includeFiles);
	}
	
	public List<TorrentDetails> getAllTorrents() throws TorrentClientException {
		return getTorrentClient().getAllTorrents();
	}
	
}
