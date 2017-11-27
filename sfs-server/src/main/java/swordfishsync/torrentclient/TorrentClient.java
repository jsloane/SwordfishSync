package swordfishsync.torrentclient;

import java.util.List;

import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;
import swordfishsync.model.TorrentDetails;
import swordfishsync.exceptions.TorrentClientException;

public interface TorrentClient {
	void addTorrent(TorrentState torrentState) throws TorrentClientException;
	void moveTorrent(Torrent torrent, String directory) throws TorrentClientException;
	void removeTorrent(Torrent torrent, Boolean deleteData) throws TorrentClientException;
	TorrentDetails getTorrentDetails(Torrent torrent, Boolean includeFiles) throws TorrentClientException;
	List<TorrentDetails> getAllTorrents() throws TorrentClientException;
}
