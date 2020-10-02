package swordfishsync.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ca.benow.transmission.TransmissionClient;
import swordfishsync.torrentclient.TorrentClient;
import swordfishsync.torrentclient.impl.TransmissionTorrentClient;
import swordfishsync.domain.Message;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;
import swordfishsync.exceptions.TorrentClientException;
import swordfishsync.model.TorrentDetails;
import swordfishsync.repository.TorrentStateRepository;

@Service("torrentClientService")
public class TorrentClientService {

    private static final Logger log = LoggerFactory.getLogger(TorrentClientService.class);
    
	TorrentClient torrentClient;

	@Resource
	SettingService settingService;
	
	@Resource
	SyncService syncService;
	
	@Resource
	MessageService messageService;

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
	
	public void setTorrentClient() {
		log.info("### TorrentClientService setTorrentClient ###");

		String torrentClientType = settingService.getValue(SettingService.CODE_TORRENT_TYPE, String.class);

		if ("transmission".equals(torrentClientType)) {
			// transmission client
			
			torrentClient = null;
			
			TransmissionClient transmissionClient = new TransmissionClient(
				settingService.getValue(SettingService.CODE_TORRENT_HOST, String.class),
				settingService.getValue(SettingService.CODE_TORRENT_PORT, Integer.class),
				settingService.getValue(SettingService.CODE_TORRENT_USERNAME, String.class),
				settingService.getValue(SettingService.CODE_TORRENT_PASSWORD, String.class)
			);
			
			if (transmissionClient.getRpcVersion() > 0) {
				torrentClient = new TransmissionTorrentClient(transmissionClient);
			} else {
				messageService.logMessage(false, Message.Type.ERROR, Message.Category.TORRENT_CLIENT, null, null,
						"Error setting Transmission Torrent Client, RPC version [" + transmissionClient.getRpcVersion() + "] not supported.");
			}
		} else {
			messageService.logMessage(false, Message.Type.ERROR, Message.Category.TORRENT_CLIENT, null, null,
					"Error; Torrent Client [" + torrentClientType + "] not supported.");
		}
		
	}

	public boolean addTorrent(TorrentState torrentState) throws TorrentClientException {
		log.info("Adding torrent [" + torrentState.getTorrent().getName() + "]");
		
		boolean added = false;
		
		// check that torrent is not already added to torrent client
		if (!torrentState.getTorrent().getAddedToTorrentClient()) {
			getTorrentClient().addTorrent(torrentState);
			torrentState.getTorrent().setAddedToTorrentClient(true);
			syncService.setTorrentStatus(torrentState.getFeedProvider(), torrentState.getTorrent(), torrentState, TorrentState.Status.IN_PROGRESS);
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
