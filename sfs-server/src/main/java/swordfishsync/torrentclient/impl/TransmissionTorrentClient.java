package swordfishsync.torrentclient.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import ca.benow.transmission.AddTorrentParameters;
import ca.benow.transmission.SetTorrentParameters;
import ca.benow.transmission.TransmissionClient;
import ca.benow.transmission.model.AddedTorrentInfo;
import ca.benow.transmission.model.TorrentStatus;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;
import swordfishsync.exceptions.TorrentClientException;
import swordfishsync.model.TorrentDetails;
import swordfishsync.torrentclient.TorrentClient;

public class TransmissionTorrentClient implements TorrentClient {

	TransmissionClient transmissionClient;
	Cache<String, List> torrentClientCache;

	// todo - recover from transmission unavailability - test if this happens
	
	public TransmissionTorrentClient(TransmissionClient transmissionClient) {
		this.transmissionClient = transmissionClient;
	
		// store torrent data in short term cache
		//String cacheKey = "transmissionClientData-" + Setting.valueFor("torrent.host") + ":" + Setting.valueFor("torrent.port").toString();
		String cacheName = "swordfishsync-transmissionClientData";

		// store torrent details in cache
		//Cache cache = MyMediaLifecycle.cacheManager.getCache("torrentClientData");
		CachingProvider provider = Caching.getCachingProvider();
	    CacheManager cacheManager = provider.getCacheManager();
	    MutableConfiguration<String, List> configuration =
	            new MutableConfiguration<String, List>()
	                .setTypes(String.class, List.class)
	                .setStoreByValue(false)
	                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));
		torrentClientCache = cacheManager.createCache(cacheName, configuration);
	}
	/*@PostConstruct
	public void init() {
	// only works in spring service/component
    }*/
	
	@Override
	public void addTorrent(TorrentState torrentState) throws TorrentClientException {
		AddTorrentParameters newTorrentParameters = new AddTorrentParameters(torrentState.getTorrent().getUrl());
		try {
			AddedTorrentInfo ati = transmissionClient.addTorrent(newTorrentParameters);
			
			// set torrent details
			torrentState.getTorrent().setHashString(ati.getHashString());
			torrentState.getTorrent().setClientTorrentId(ati.getId());
			if (torrentState.getTorrent().getName() == null) {
				torrentState.getTorrent().setName(ati.getName());
			}
			
			if (torrentState.getFeedProvider().getUploadLimit() > 0) {
				// set upload limit
				SetTorrentParameters setTorrentParameters = new SetTorrentParameters(ati.getId());
				setTorrentParameters.setUploadLimit(torrentState.getFeedProvider().getUploadLimit());
				transmissionClient.setTorrents(setTorrentParameters);
			}
		} catch (IOException e) {
			throw new TorrentClientException("An error occurring adding the torrent to Transmission: " + e.getMessage(), e);
		}
	}

	@Override
	public void moveTorrent(Torrent torrent, String directory) throws TorrentClientException {
		try {
			transmissionClient.moveTorrents(new Object[] {torrent.getHashString()}, directory, true);
		} catch (IOException e) {
			throw new TorrentClientException(String.format("Error moving torrent [%s]", torrent.getName()), e);
		}
	}

	@Override
	public void removeTorrent(Torrent torrent, Boolean deleteData) throws TorrentClientException {
		try {
			transmissionClient.removeTorrents(new Object[] {torrent.getHashString()}, deleteData);
		} catch (IOException e) {
			throw new TorrentClientException(String.format("Error removing torrent [%s]", torrent.getName()), e);
		}
	}

	@Override
	public TorrentDetails getTorrentDetails(Torrent torrent, Boolean includeFiles) throws TorrentClientException {
		TorrentDetails torrentDetails = new TorrentDetails();
		torrentDetails.setStatus(TorrentDetails.Status.UNKNOWN);
		List<TorrentStatus> torrentStatuses = new ArrayList<TorrentStatus>();
		
		String cacheKey = "allTorrents";
	    
	    /*Element torrentStatusesElement = null;
	    if (cache != null) {
	    	torrentStatusesElement = cache.get("torrentStatuses");
	    }*/
	    
		//Cache torrentClientCache = grailsCacheManager.getCache('torrentClient')
		
		System.out.println("torrentClientCache: " + torrentClientCache);
		
		//Date currentDate = new Date();
		if (torrentClientCache != null) {
			// get cached data if available
			/*Cache.ValueWrapper cachedDateOfCache = torrentClientCache.get(cacheKey + "-time")
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
			}*/
			torrentStatuses = torrentClientCache.get(cacheKey);
		}
		//torrentStatuses = null
		
		if (torrentStatuses == null) {
			//println '### getting torrentStatuses from transmission ####################################################################'
			try {
				torrentStatuses = transmissionClient.getAllTorrents(
					/*(TorrentStatus.TorrentField[]) [
						TorrentStatus.TorrentField.id,
						TorrentStatus.TorrentField.activityDate,
						TorrentStatus.TorrentField.status,
						TorrentStatus.TorrentField.hashString,
						TorrentStatus.TorrentField.files,
						TorrentStatus.TorrentField.percentDone,
						TorrentStatus.TorrentField.downloadDir
					]*/
					new TorrentStatus.TorrentField[] {
						TorrentStatus.TorrentField.id,
						TorrentStatus.TorrentField.activityDate,
						TorrentStatus.TorrentField.status,
						TorrentStatus.TorrentField.hashString,
						TorrentStatus.TorrentField.files,
						TorrentStatus.TorrentField.percentDone,
						TorrentStatus.TorrentField.downloadDir
					}
				);
				
				if (torrentClientCache != null && torrentStatuses != null) {
					torrentClientCache.put(cacheKey, torrentStatuses);
				}
			} catch (IOException e) {
				throw new TorrentClientException("Error getting torrent details for torrent [" + torrent.getName() + "]", e);
				//log.error('Error fetching and caching torrent status', e)
				//e.printStackTrace()
			}
		}
		
		// hashstring being deleted by quartz job
		if (torrent != null) {
			for (TorrentStatus torrentStatus : torrentStatuses) {
				//println 'torrentStatus.getField(TorrentStatus.TorrentField.hashString): ' + torrentStatus.getField(TorrentStatus.TorrentField.hashString)
				if (torrentStatus != null && StringUtils.isNotBlank(torrent.getHashString()) && torrent.getHashString().equals(torrentStatus.getField(TorrentStatus.TorrentField.hashString))) {
					// set status
					//println 'FOUND TORRENT STATUS: ' + torrentStatus
					torrentDetails = setTorrentDetails(includeFiles, torrentStatus);
					break;
				}
			}
		}
		
		return torrentDetails;
	}

	private TorrentDetails setTorrentDetails(Boolean includeFiles, TorrentStatus torrentStatus) {
		TorrentDetails torrentDetails = new TorrentDetails();
		
		// set status
		switch(torrentStatus.getStatus()) {
			case downloadWait:
				torrentDetails.setStatus(TorrentDetails.Status.QUEUED);
				break;
			case downloading:
				torrentDetails.setStatus(TorrentDetails.Status.DOWNLOADING);
				break;
			case seedWait:
				torrentDetails.setStatus(TorrentDetails.Status.SEEDWAIT);
				break;
			case seeding:
				torrentDetails.setStatus(TorrentDetails.Status.SEEDING);
				break;
			case finished:
				torrentDetails.setStatus(TorrentDetails.Status.FINISHED);
				break;
			default:
				torrentDetails.setStatus(TorrentDetails.Status.UNKNOWN);
				break;
		}
		
		// set downloaded directory
		torrentDetails.setDownloadedToDirectory(torrentStatus.getField(TorrentStatus.TorrentField.downloadDir).toString());
		
		// set percent done
		torrentDetails.setPercentDone(torrentStatus.getPercentDone());
		
		// set files
		if (includeFiles) {
			JSONTokener tokener = new JSONTokener(torrentStatus.getField(TorrentStatus.TorrentField.files).toString());
			JSONArray fileArray = new JSONArray(tokener);
			
			for (int i = 0; i < fileArray.length(); i++) {
				JSONObject obj = new JSONObject(fileArray.get(i).toString());
				String filename = obj.getString("name");
				torrentDetails.getFiles().add(filename);
			}
		}
		
		return torrentDetails;
	}

	public List<TorrentDetails> getAllTorrents() throws TorrentClientException {
		List<TorrentDetails> allTorrents = new ArrayList<TorrentDetails>();
		try {
			List<TorrentStatus> allTorrentStatuses = transmissionClient.getAllTorrents(
					new TorrentStatus.TorrentField[] {
							TorrentStatus.TorrentField.all
					}
			);
			for (TorrentStatus torrentStatus : allTorrentStatuses) {
				TorrentDetails torrentDetails = setTorrentDetails(false, torrentStatus);
				torrentDetails.setName(torrentStatus.getField(TorrentStatus.TorrentField.name).toString());
				Integer activityDate = (Integer) torrentStatus.getField(TorrentStatus.TorrentField.activityDate);
				if (activityDate != null && activityDate > 0) {
					torrentDetails.setActivityDate(new Date(activityDate * 1000L));
				}
				allTorrents.add(torrentDetails);
			}
		} catch (IOException e) {
			throw new TorrentClientException("Error getting all torrents", e);
		}
		return allTorrents;
	}
	
}
