package swordfishsync

import grails.plugin.cache.GrailsCacheManager
import java.io.IOException
import java.util.List
import java.util.logging.Level;

import ca.benow.transmission.AddTorrentParameters
import ca.benow.transmission.SetTorrentParameters
import ca.benow.transmission.TransmissionClient
import ca.benow.transmission.model.AddedTorrentInfo
import ca.benow.transmission.model.TorrentStatus
import swordfishsync.exceptions.TorrentClientException

import org.json.JSONArray
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.cache.Cache

import grails.util.GrailsStringUtils

class TransmissionTorrentClient implements TorrentClient {
	
	GrailsCacheManager grailsCacheManager
	TransmissionClient transmissionClient
	
	def addTorrent(FeedProvider feedProvider, Torrent torrent) throws TorrentClientException {
		AddTorrentParameters newTorrentParameters = new AddTorrentParameters(torrent.url)
		//newTorrentParameters.setPeerLimit(0); // testing with no peers
		try {
			AddedTorrentInfo ati = transmissionClient.addTorrent(newTorrentParameters)
			
			// set torrent details
			torrent.hashString = ati.getHashString()
			torrent.clientTorrentId = ati.getId()
			torrent.status = Torrent.Status.IN_PROGRESS
			
			if (feedProvider.uploadLimit > 0) {
				// set upload limit
				SetTorrentParameters setTorrentParameters = new SetTorrentParameters(ati.getId())
				setTorrentParameters.setUploadLimit(feedProvider.uploadLimit)
				transmissionClient.setTorrents(setTorrentParameters)
			}
		} catch (IOException e) {
			throw new TorrentClientException('An error occurring adding the torrent to Transmission', e)
		}
	}
	
	def moveTorrent(Torrent torrent, String directory) throws TorrentClientException {
		try {
			transmissionClient.moveTorrents((Object[]) [torrent.hashString], directory, true)
		} catch (IOException e) {
			throw new TorrentClientException('Error moving torrent', e)
		}
	}
	
	def removeTorrent(Torrent torrent, Boolean deleteData) throws TorrentClientException {
		try {
			transmissionClient.removeTorrents((Object[]) [torrent.hashString], deleteData)
		} catch (IOException e) {
			throw new TorrentClientException('Error removing torrent', e)
		}
	}
	
	TorrentDetails getTorrentDetails(Torrent torrent, Boolean includeFiles) throws TorrentClientException {
		TorrentDetails torrentDetails = new TorrentDetails(status: TorrentDetails.Status.UNKNOWN)
		//TorrentClientService.TorrentStatus foundTorrentStatus = TorrentClientService.TorrentStatus.UNKNOWN
		List<TorrentStatus> torrentStatuses = []
		
		// store torrent data in short term cache
		String cacheKey = 'transmissionClientData-' + 'hostname:port' //todo: get hostname + port
		Cache torrentClientCache = grailsCacheManager.getCache('torrentClient')
		if (torrentClientCache) {
			Cache.ValueWrapper cachedData = torrentClientCache.get(cacheKey)
			if (cachedData) {
				println '############## GETTING torrentStatus from cache'
				torrentStatuses = (List<TorrentStatus>) cachedData.get()
			}
		}
		
		if (!torrentStatuses) {
			println '############## GETTING torrentStatus from TRANSMISSION'
			try {
				torrentStatuses = transmissionClient.getAllTorrents(
					(TorrentStatus.TorrentField[]) [
						TorrentStatus.TorrentField.id,
						TorrentStatus.TorrentField.activityDate,
						TorrentStatus.TorrentField.status,
						TorrentStatus.TorrentField.hashString,
						TorrentStatus.TorrentField.files,
						TorrentStatus.TorrentField.percentDone,
						TorrentStatus.TorrentField.downloadDir
					]
				)
				
				if (torrentClientCache && torrentStatuses) {
					torrentClientCache.put(cacheKey, torrentStatuses)
				}
			} catch (IOException e) {
				// todo: set error
				//log.error('Error fetching and caching torrent status', e)
				e.printStackTrace()
			}
		}
		
		for (TorrentStatus torrentStatus : torrentStatuses) {
			if (torrentStatus != null && GrailsStringUtils.isNotBlank(torrent.hashString) && torrent.hashString.equals(torrentStatus.getField(TorrentStatus.TorrentField.hashString))) {
				// set status
				switch(torrentStatus.getStatus()) {
					case TorrentStatus.StatusField.seeding:
						torrentDetails.status = TorrentDetails.Status.SEEDING
						break;
					case TorrentStatus.StatusField.seedWait:
						torrentDetails.status = TorrentDetails.Status.SEEDWAIT
						break;
					case TorrentStatus.StatusField.finished:
						torrentDetails.status = TorrentDetails.Status.FINISHED
						break;
				}
				
				// set downloaded directory
				torrentDetails.downloadedToDirectory = torrentStatus.getField(TorrentStatus.TorrentField.downloadDir).toString()
				
				// set files
				if (includeFiles) {
					JSONTokener tokener = new JSONTokener(torrentStatus.getField(TorrentStatus.TorrentField.files).toString())
					JSONArray fileArray = new JSONArray(tokener)
					
					for (int i = 0; i < fileArray.length(); i++) {
						JSONObject obj = new JSONObject(fileArray.get(i).toString())
						String filename = obj.getString('name')
						torrentDetails.files.add(filename)
					}
				}
				
				break
			}
		}
		
		return torrentDetails
	}
	
}