package swordfishsync

import swordfishsync.exceptions.TorrentClientException

interface TorrentClient {
	def addTorrent(FeedProvider feedProvider, Torrent torrent) throws TorrentClientException
	def moveTorrent(Torrent torrent, String directory) throws TorrentClientException
	def removeTorrent(Torrent torrent, Boolean deleteData) throws TorrentClientException
	TorrentDetails getTorrentDetails(Torrent torrent, Boolean includeFiles) throws TorrentClientException
}
