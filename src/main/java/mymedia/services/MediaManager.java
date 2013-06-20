package mymedia.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;

import com.github.junrar.extract.ExtractArchive;

import ca.benow.transmission.AddTorrentParameters;
import ca.benow.transmission.SetTorrentParameters;
import ca.benow.transmission.TorrentParameters;
import ca.benow.transmission.TransmissionClient;
import ca.benow.transmission.model.AddedTorrentInfo;
import ca.benow.transmission.model.TorrentStatus;
import ca.benow.transmission.model.TorrentStatus.StatusField;
import ca.benow.transmission.model.TorrentStatus.TorrentField;

import mymedia.db.form.FilterAttribute;
import mymedia.db.form.TorrentInfo;
import mymedia.db.service.FeedInfoService;
import mymedia.db.service.TorrentInfoService;
import mymedia.services.model.FeedProvider;
import mymedia.services.model.MediaInfo;
import mymedia.util.EmailManager;

public class MediaManager {

	private final static Logger Log = Logger.getLogger(MyMediaLifecycle.class.getName());
	
    public static FeedInfoService feedInfoService;
    public static TorrentInfoService torrentInfoService;
	public static List<FeedProvider> feedProviders;
	public static TransmissionClient torrentClient;
    public static EmailManager mailManager;
	public static boolean debug = false;
	//public static boolean isSyncingFeeds = false; // avoid concurrent editing of objects, or use hibernate services  //
	
	public static void syncTorrents() {
		//isSyncingFeeds = true;
		for (FeedProvider feedProvider : feedProviders) {
			syncTorrentFeed(feedProvider);
		}
		//isSyncingFeeds = false;
	}
	
	public static void syncTorrentFeed(FeedProvider feedProvider) {
		List<TorrentInfo> torrents = new ArrayList<TorrentInfo>(feedProvider.getTorrents()); // create a copy of the torrent list, as it'll be modified during the loop
		List<TorrentInfo> torrentsFromFeed = feedProvider.getTorrentsFromFeed();

		// for debug stuff
		if (debug) {
			debugMethod(feedProvider, torrents);
			return;
		}

		int t = 0;
		for (TorrentInfo torrent : torrents) {
			if (torrent != null) {
				boolean saveTorrent = true;
				switch (torrent.getStatus()) {
					case TorrentInfo.STATUS_NOT_ADDED: // new torrent, add to torrent client
						checkAndAdd(feedProvider, torrent);
						break;
					case TorrentInfo.STATUS_IN_PROGRESS: // downloading/seeding torrent, check if finished
						checkAndComplete(feedProvider, torrent);
						break;
					 case TorrentInfo.STATUS_NOTIFY_COMPLETED: // retry sending notification that download has completed, should already be done but just in case
						notifyComplete(feedProvider, torrent, new MediaInfo(torrent));
						break;
					case TorrentInfo.STATUS_NOTIFIED_NOT_ADDED: // notification has been sent for this torrent, no longer need the record
						saveTorrent = checkAndRemove(feedProvider, torrent, torrentsFromFeed);
						break;
					case TorrentInfo.STATUS_SKIPPED: // torrent skipped, don't need the record
						saveTorrent = checkAndRemove(feedProvider, torrent, torrentsFromFeed);
						break;
					case TorrentInfo.STATUS_COMPLETED: // completed torrent, remove the record
						saveTorrent = checkAndRemove(feedProvider, torrent, torrentsFromFeed);
						break;
				}
				if (saveTorrent) {
					feedProvider.saveTorrent(torrent);
				}
			}
		}
	}

	private static void checkAndAdd(FeedProvider feedProvider, TorrentInfo torrentInfo) {
		if (feedProvider.getFeedInfo().getActive()) {
			if (!feedProvider.getFeedInfo().getEnableFilter() || feedProvider.checkFilterMatch(torrentInfo.getName())) {
				if (feedProvider.getFeedInfo().getAction().equalsIgnoreCase("download")) {
					// if download
					addTorrent(feedProvider, torrentInfo);
				} else if (feedProvider.getFeedInfo().getAction().equalsIgnoreCase("notify")) {
					// if notify only
					notifyNew(feedProvider, torrentInfo, new MediaInfo(torrentInfo));
				}
			} else {
				// not interested in this torrent, set to skipped
				torrentInfo.setStatus(TorrentInfo.STATUS_SKIPPED);
			}
		}
	}

	private static boolean checkAndRemove(FeedProvider feedProvider, TorrentInfo torrent, List<TorrentInfo> torrentsFromFeed) {
		boolean saveTorrent = false;
		if (!feedProvider.isFeedCurrent()) { // || !feedProvider.getKeepTorrentHistory()...
			return saveTorrent;
		}
		if (torrent.getDateCompleted() != null) {
			boolean torrentInFeed = false;
			for (TorrentInfo torrentFromFeed : torrentsFromFeed) {
				if (torrentFromFeed.getName().equals(torrent.getName()) && torrentFromFeed.getUrl().equals(torrent.getUrl())) {
					torrentInFeed = true;
				}		
			}
			
			long completedInterval = TimeUnit.MILLISECONDS.toDays(new Date().getTime() - torrent.getDateCompleted().getTime());
			if (!torrentInFeed && completedInterval > feedProvider.getFeedInfo().getDeleteInterval()) {
				Log.log(Level.INFO, "[DEBUG] Deleting torrent \"" + torrent.getName() + "\" from db, dateCompleted: "
						+ torrent.getDateCompleted() + ", " + completedInterval + " days ago");
				feedProvider.removeTorrent(torrent);
			}
		} else {
			saveTorrent = true;
			torrent.setDateCompleted(new Date());
		}
		return saveTorrent;
	}

	private static void addTorrent(FeedProvider feedProvider, TorrentInfo torrent) {
		Log.log(Level.INFO, "[DEBUG] MediaManager.addTorrent - adding torrent: " + torrent);
		try {
			// add torrent
			AddTorrentParameters newTorrentParameters = new AddTorrentParameters(torrent.getUrl());
			//newTorrentParameters.setPeerLimit(-1);
			//newTorrentParameters.setPeerLimit(0); // testing with no peers
			AddedTorrentInfo ati = torrentClient.addTorrent(newTorrentParameters);
			
			torrent.setHashString(ati.getHashString());
			torrent.setClientTorrentId(ati.getId());
			torrent.setStatus(TorrentInfo.STATUS_IN_PROGRESS);
			
			if (feedProvider.getFeedInfo().getUploadLimit() > 0) {
				// set upload limit
				SetTorrentParameters setTorrentParameters = new SetTorrentParameters(ati.getId());
				setTorrentParameters.setUploadLimit(feedProvider.getFeedInfo().getUploadLimit());
				torrentClient.setTorrents(setTorrentParameters);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void checkAndComplete(FeedProvider feedProvider, TorrentInfo torrentInfo) {
		
		// move and remove torrent
		try {
			TorrentStatus torrentStatus = getTorrentStatus(torrentInfo);
			// check if torrent is completed
			if (torrentStatus != null && (torrentStatus.getStatus() == StatusField.seeding || torrentStatus.getStatus() == StatusField.seedWait)) {
				Log.log(Level.INFO, "[DEBUG] MediaManager.checkAndComplete Torrent download completed for: " + torrentInfo);
				
				MediaInfo mediaInfo = new MediaInfo(torrentInfo);
				
				if (feedProvider.getFeedInfo().getDownloadDirectory() != null && !feedProvider.getFeedInfo().getDownloadDirectory().isEmpty()) {
					String downloadDir = constructDownloadDirectory(feedProvider, mediaInfo);
					//baseDir = "M:/Video/TestDir/"; // TESTING
					String torrentDownloadDir = torrentStatus.getField(TorrentField.downloadDir).toString();
					//torrentDownloadDir = "M:/torrents/torrentdownloads"; // TESTING
					if (!torrentDownloadDir.endsWith("/")) {
						torrentDownloadDir += "/";
					}
					
					JSONTokener tokener = new JSONTokener(torrentStatus.getField(TorrentField.files).toString());
					JSONArray fileArray = new JSONArray(tokener);
					
					// check if the torrent is a rar to extract
					boolean unrared = false;
					if (feedProvider.getFeedInfo().getExtractRars()) {
						for (int i = 0; i < fileArray.length(); i++) {
							JSONObject obj = new JSONObject(fileArray.get(i).toString());
							String filename = obj.getString("name");
							if (filename.endsWith(".rar")) {
								System.out.println("[DEBUG] found rar file to extract: " + torrentDownloadDir + filename);
								Log.log(Level.INFO, "[DEBUG] MediaManager.checkAndComplete unraring archive to: " + downloadDir);
								
								// extract rar file
								extractRar(torrentDownloadDir + filename, downloadDir); // overwrites existing files
								unrared = true;
							}
						}
					}
					
					// if not a rar archive, just copy the entire torrent
					if (!unrared) {
						if (feedProvider.getFeedInfo().getRemoveTorrentOnComplete()) {
							// just move torrent files using transmission
							Log.log(Level.INFO, "[DEBUG] MediaManager.checkAndComplete moving torrent to: " + downloadDir);
							torrentClient.moveTorrents(new Object[] {torrentStatus.getId()}, downloadDir, true);
						} else {
							// copy torrent files to downloadDir
							Log.log(Level.INFO, "[DEBUG] MediaManager.checkAndComplete copying torrent files to: " + downloadDir);

							System.out.println("[DEBUG] COPYING FILES...");
							for (int i = 0; i < fileArray.length(); i++) {
								JSONObject obj = new JSONObject(fileArray.get(i).toString());
								String filename = obj.getString("name");
								FileUtils.copyFileToDirectory(new File(torrentDownloadDir + filename), new File(downloadDir)); // overwrites existing files
							}
							System.out.println("[DEBUG] ...DONE");
						}
					}
				}
				
				
				
				// checkRemoveRules() // do torrentClient.removeTorrents in this method..., or logic block
				// seed ratios, activity dates, minimum seed time, etc...
				if (feedProvider.getFeedInfo().getRemoveTorrentOnComplete()) {
					Log.log(Level.INFO, "[DEBUG] MediaManager.checkAndComplete removing torrent from torrent client");
					torrentClient.removeTorrents(new Object[] {torrentStatus.getId()}, false); // DON'T DELETE TORRENT DATA
				}
				
				
				
				torrentInfo.setStatus(TorrentInfo.STATUS_NOTIFY_COMPLETED);
				torrentInfo.setDateCompleted(new Date());
				
				notifyComplete(feedProvider, torrentInfo, mediaInfo);
			}
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String constructDownloadDirectory(FeedProvider feedProvider, MediaInfo mediaInfo) {
		
		// need to check if media is HD, and use HD directory...
		
		String baseDir = feedProvider.getFeedInfo().getDownloadDirectory();
		if (!baseDir.endsWith("/")) {
			baseDir += "/";
		}
		if (feedProvider.getFeedInfo().getDetermineSubDirectory()) {
			baseDir = baseDir + mediaInfo.subDirectory;
		}
		return baseDir;
	}
	
	private static void notifyNew(FeedProvider feedProvider, TorrentInfo torrentInfo, MediaInfo mediaInfo) {
		Log.log(Level.INFO, "[DEBUG] MediaManager.notifyNew for new torrent: " + torrentInfo);
		if (feedProvider.getFeedInfo().getNotifyEmail() != null && !feedProvider.getFeedInfo().getNotifyEmail().isEmpty()) {
			// send email notification
			try {
				mailManager.sendMail(feedProvider, torrentInfo, mediaInfo, "new");
				torrentInfo.setStatus(TorrentInfo.STATUS_NOTIFIED_NOT_ADDED);
			} catch (MailException | MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// other notify methods
		else {
			
		}
	}
	
	private static void notifyComplete(FeedProvider feedProvider, TorrentInfo torrentInfo, MediaInfo mediaInfo) {
		if (feedProvider.getFeedInfo().getNotifyEmail() != null && !feedProvider.getFeedInfo().getNotifyEmail().isEmpty()) {
			Log.log(Level.INFO, "[DEBUG] MediaManager.notifyComplete for completed torrent: " + torrentInfo);
			// send email notification
			try {
				mailManager.sendMail(feedProvider, torrentInfo, mediaInfo, "completed");
				torrentInfo.setStatus(TorrentInfo.STATUS_COMPLETED);
			} catch (MailException | MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// other notify methods
		else {
			torrentInfo.setStatus(TorrentInfo.STATUS_COMPLETED);
		}
	}
	
	private static TorrentStatus getTorrentStatus(TorrentInfo torrent) throws IOException, JSONException {
		TorrentStatus foundTorrentStatus = null;
		//List<TorrentStatus> torrentStatuses = torrentClient.getAllTorrents( // can't rely on torrent id if torrent removed/added from another client, so get all torrents...
		//System.out.println("torrent.getClientTorrentId(): " + torrent.getClientTorrentId());
		List<TorrentStatus> torrentStatuses = torrentClient.getTorrents(
			new int[] {torrent.getClientTorrentId()},
			new TorrentStatus.TorrentField[] {
				TorrentStatus.TorrentField.id,
				TorrentStatus.TorrentField.activityDate,
				TorrentStatus.TorrentField.status,
				TorrentStatus.TorrentField.hashString,
				TorrentStatus.TorrentField.files,
				TorrentStatus.TorrentField.downloadDir
			}
		);
		
		for (TorrentStatus torrentStatus : torrentStatuses) {
			if (torrentStatus != null && torrent.getHashString().equals(torrentStatus.getField(TorrentStatus.TorrentField.hashString))) {
				foundTorrentStatus = torrentStatus;
			}
		}
		return foundTorrentStatus;
	}
	
	public static List<TorrentStatus> getAllTorrentStatus() throws IOException, JSONException {
		return torrentClient.getAllTorrents(
			new TorrentStatus.TorrentField[] {
				TorrentStatus.TorrentField.all
			}
		);
	}
	
	public static void extractRar(String filename, String destinationDir) throws IOException {
		// SPAWN NEW THREAD? maybe not if deleting torrent...
		System.out.println("[DEBUG] extractRar filename: " + filename);
		System.out.println("[DEBUG] extractRar destinationDir: " + destinationDir);
		
		// create destinationDir if it doesn't exist
		File saveDir = new File(destinationDir);
		if(!saveDir.exists()) {
			boolean createdDir  = saveDir.mkdirs();
			if (!createdDir) {
				throw new IOException("Unable to create directory: " + saveDir);
			}
		}
		
		System.out.println("[DEBUG] EXTRACTING...");
		final File rar = new File(filename);
		final File destinationFolder = new File(destinationDir);
		ExtractArchive extractArchive = new ExtractArchive();
		extractArchive.extractArchive(rar, destinationFolder);
		System.out.println("[DEBUG] ...DONE");
	}
	
	
	
	private static void debugMethod(FeedProvider feedProvider, List<TorrentInfo> torrents) {
		
		/*int t = 0;
		for (TorrentInfo torrent : torrents) {
			t++;
			if (t == 2) {
				//break;
			}
		}*/
		
		Log.log(Level.INFO, "[MYMEDIA] MediaManager.debug: "+ debug + ", skipping torrent checks.");
		
	}
}
