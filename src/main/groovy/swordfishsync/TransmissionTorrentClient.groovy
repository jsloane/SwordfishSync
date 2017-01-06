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
	FeedService feedService
	
	// todo - recover from transmission unavailability - test if this happens
	
	def addTorrent(FeedProvider feedProvider, Torrent torrent) throws TorrentClientException {
		AddTorrentParameters newTorrentParameters = new AddTorrentParameters(torrent.url)
		//newTorrentParameters.setPeerLimit(0); // testing with no peers
		try {
			AddedTorrentInfo ati = transmissionClient.addTorrent(newTorrentParameters)
			
			// set torrent details
			torrent.hashString = ati.getHashString()
			torrent.clientTorrentId = ati.getId()
			if (!torrent.name) {
				torrent.name = ati.getName()
			}
			
			if (feedProvider.uploadLimit > 0) {
				// set upload limit
				SetTorrentParameters setTorrentParameters = new SetTorrentParameters(ati.getId())
				setTorrentParameters.setUploadLimit(feedProvider.uploadLimit)
				transmissionClient.setTorrents(setTorrentParameters)
			}
		} catch (IOException e) {
			throw new TorrentClientException('An error occurring adding the torrent to Transmission: ' + e.message, e)
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
	
	TorrentDetails setTorrentDetails(Boolean includeFiles, TorrentStatus torrentStatus) {
		TorrentDetails torrentDetails = new TorrentDetails(status: TorrentDetails.Status.UNKNOWN)
		
		// set status
		switch(torrentStatus.getStatus()) {
			case TorrentStatus.StatusField.downloadWait:
				torrentDetails.status = TorrentDetails.Status.QUEUED
				break;
			case TorrentStatus.StatusField.downloading:
				torrentDetails.status = TorrentDetails.Status.DOWNLOADING
				break;
			case TorrentStatus.StatusField.seedWait:
				torrentDetails.status = TorrentDetails.Status.SEEDWAIT
				break;
			case TorrentStatus.StatusField.seeding:
				torrentDetails.status = TorrentDetails.Status.SEEDING
				break;
			case TorrentStatus.StatusField.finished:
				torrentDetails.status = TorrentDetails.Status.FINISHED
				break;
		}
		
		// set downloaded directory
		torrentDetails.downloadedToDirectory = torrentStatus.getField(TorrentStatus.TorrentField.downloadDir).toString()
		
		// set percent done
		torrentDetails.percentDone = torrentStatus.getPercentDone()
		
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
		
		return torrentDetails
	}
	
	TorrentDetails getTorrentDetails(Torrent torrent, Boolean includeFiles) throws TorrentClientException {
		TorrentDetails torrentDetails = new TorrentDetails(status: TorrentDetails.Status.UNKNOWN)
		List<TorrentStatus> torrentStatuses = []
		
		// store torrent data in short term cache
		String cacheKey = 'transmissionClientData-' + Setting.valueFor('torrent.host') + ':' + Setting.valueFor('torrent.port').toString()
		
		Cache torrentClientCache = grailsCacheManager.getCache('torrentClient')
		
		/*println 'torrentClientCache.getNativeCache(): ' + torrentClientCache.getNativeCache()
		grailsCacheManager.cacheNames.each {
			def config = grailsCacheManager.getCache(it).nativeCache.cacheConfiguration
			println "it: ${it}"
			println "timeToLiveSeconds: ${config.timeToLiveSeconds}"
			println "timeToIdleSeconds: ${config.timeToIdleSeconds}"
		}*/
		
		Date currentDate = new Date()
		if (torrentClientCache) {
			// get cached data if available
			Cache.ValueWrapper cachedDateOfCache = torrentClientCache.get(cacheKey + '-time')
			if (cachedDateOfCache) {
				Date cachedDate = (Date) cachedDateOfCache.get()
				if (cachedDate) {
					def ttl = 55
					def difference = currentDate.time - cachedDate.time
					if (difference > java.util.concurrent.TimeUnit.SECONDS.toMillis(ttl)) {
						torrentClientCache.evict(cacheKey)
					}
				}
			}
			
			
			Cache.ValueWrapper cachedTorrentClientData = torrentClientCache.get(cacheKey)
			if (cachedTorrentClientData) {
				torrentStatuses = (List<TorrentStatus>) cachedTorrentClientData.get()
			}
		}
		//torrentStatuses = null
		
		if (!torrentStatuses) {
			//println '### getting torrentStatuses from transmission ####################################################################'
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
					torrentClientCache.put(cacheKey + '-time', currentDate)
				}
			} catch (IOException e) {
				throw new TorrentClientException('Error getting torrent details for torrent [' + torrent.name + ']', e)
				//log.error('Error fetching and caching torrent status', e)
				//e.printStackTrace()
			}
		}
		
		// hashstring being deleted by quartz job
		if (torrent) {
			for (TorrentStatus torrentStatus : torrentStatuses) {
				//println 'torrentStatus.getField(TorrentStatus.TorrentField.hashString): ' + torrentStatus.getField(TorrentStatus.TorrentField.hashString)
				
				if (torrentStatus != null && GrailsStringUtils.isNotBlank(torrent.hashString) && torrent.hashString.equals(torrentStatus.getField(TorrentStatus.TorrentField.hashString))) {
					// set status
					//println 'FOUND TORRENT STATUS: ' + torrentStatus
					
					torrentDetails = setTorrentDetails(includeFiles, torrentStatus)
					
					break
				}
			}
		}
		
		return torrentDetails
	}
	
	List<TorrentDetails> getAllTorrents() {
		List<TorrentDetails> allTorrents = []
		try {
			List<TorrentStatus> allTorrentStatuses = transmissionClient.getAllTorrents((TorrentStatus.TorrentField[]) [TorrentStatus.TorrentField.all])
			for (TorrentStatus torrentStatus : allTorrentStatuses) {
				TorrentDetails torrentDetails = setTorrentDetails(false, torrentStatus)
				torrentDetails.name = torrentStatus.getField(TorrentStatus.TorrentField.name).toString()
				int activityDate = torrentStatus.getField(TorrentStatus.TorrentField.activityDate)
				if (activityDate && activityDate > 0) {
					torrentDetails.activityDate = new Date(activityDate * 1000 )
				}
				allTorrents.add(torrentDetails)
			}
		} catch (IOException e) {
			throw new TorrentClientException('Error getting all torrents', e)
		}
		return allTorrents
	}
	
}
