package mymedia.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
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

	private final static Logger log = Logger.getLogger(MyMediaLifecycle.class.getName());
	
    public static FeedInfoService feedInfoService;
    public static TorrentInfoService torrentInfoService;
	public static List<FeedProvider> feedProviders;
	public static TransmissionClient torrentClient;
    public static EmailManager mailManager;
	public static boolean debug = false;
	//public static boolean isSyncingFeeds = false; // avoid concurrent editing of objects, or use hibernate services  //
	
	public static void syncTorrents() {
		//isSyncingFeeds = true;
		//System.out.println("[DEBUG] feedProviders: " + feedProviders);
		for (FeedProvider feedProvider : feedProviders) {
			syncTorrentFeed(feedProvider);
		}
		System.out.println("[DEBUG] FINISHED MediaManager.syncTorrents()");
		//isSyncingFeeds = false;
	}
	
	public static void syncTorrentFeed(FeedProvider feedProvider) {
		// for debug stuff
		if (debug) {
			debugMethod(feedProvider);
			return;
		}
		
		if (!feedProvider.getFeedInfo().getActive()) {
			return;
		}
		
		List<TorrentInfo> torrents = new ArrayList<TorrentInfo>(feedProvider.getTorrents()); // create a copy of the torrent list, as it'll be modified during the loop
		List<TorrentInfo> torrentsFromFeed = feedProvider.getTorrentsFromFeed();
		
		for (TorrentInfo torrent : torrents) {
			if (torrent != null) {
				boolean saveTorrent = true;
				switch (torrent.getStatus()) {
					case TorrentInfo.STATUS_NOT_ADDED: // new torrent, add to torrent client
						saveTorrent = checkAndAdd(feedProvider, torrent);
						break;
					case TorrentInfo.STATUS_IN_PROGRESS: // downloading/seeding torrent, check if finished
						checkAndComplete(feedProvider, torrent);
						break;
					 case TorrentInfo.STATUS_NOTIFY_COMPLETED: // retry sending notification that download has completed, should already be done but just in case
						notifyComplete(feedProvider, torrent, new MediaInfo(feedProvider, torrent));
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
	
	private static boolean checkAndAdd(FeedProvider feedProvider, TorrentInfo torrentInfo) {
		if (feedProvider.getFeedInfo().getActive()) {
			if (feedProvider.shouldAddTorrent(torrentInfo)) {
				if (feedProvider.getFeedInfo().getAction().equalsIgnoreCase("download")) {
					// if download
					//try {
						addTorrent(feedProvider, torrentInfo);
					//} catch (Exception e) {
						
					//}
				} else if (feedProvider.getFeedInfo().getAction().equalsIgnoreCase("notify")) {
					// if notify only
					notifyNew(feedProvider, torrentInfo, new MediaInfo(feedProvider, torrentInfo));
				}
			} else {
				// not interested in this torrent, set to skipped
				torrentInfo.setStatus(TorrentInfo.STATUS_SKIPPED);
				feedProvider.saveTorrent(torrentInfo);
			}
		}
		return false;
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
					break;
				}
			}
			
			long completedInterval = TimeUnit.MILLISECONDS.toDays(new Date().getTime() - torrent.getDateCompleted().getTime());
			if (!torrentInFeed && completedInterval > feedProvider.getFeedInfo().getDeleteInterval() && feedProvider.getFeedInfo().getDeleteInterval() > 0) {
				log.log(Level.INFO, "[DEBUG] Deleting torrent \"" + torrent.getName() + "\" from db, dateCompleted: "
						+ torrent.getDateCompleted() + ", " + completedInterval + " days ago");
				feedProvider.removeTorrent(torrent);
			}
		} else {
			saveTorrent = true;
			torrent.setDateCompleted(new Date()); // remove next pass
		}
		return saveTorrent;
	}
	
	public static AddedTorrentInfo addTorrent(FeedProvider feedProvider, TorrentInfo torrentInfo) {
		log.log(Level.INFO, "[DEBUG] MediaManager.addTorrent - adding torrent: " + torrentInfo);
		AddedTorrentInfo ati = null;
		try {
			// add torrent
			AddTorrentParameters newTorrentParameters = new AddTorrentParameters(torrentInfo.getUrl());
			//newTorrentParameters.setPeerLimit(-1);
			//newTorrentParameters.setPeerLimit(0); // testing with no peers
			ati = torrentClient.addTorrent(newTorrentParameters);
			
			// set torrent details
			torrentInfo.setHashString(ati.getHashString());
			torrentInfo.setClientTorrentId(ati.getId());
			torrentInfo.setStatus(TorrentInfo.STATUS_IN_PROGRESS);
			
			if (feedProvider.getFeedInfo().getUploadLimit() > 0) {
				// set upload limit
				SetTorrentParameters setTorrentParameters = new SetTorrentParameters(ati.getId());
				setTorrentParameters.setUploadLimit(feedProvider.getFeedInfo().getUploadLimit());
				torrentClient.setTorrents(setTorrentParameters);
			}
		} catch (Exception e) {
			System.out.println("TEST - MediaManager.addTorrent");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		feedProvider.saveTorrent(torrentInfo);
		return ati;
	}
	
	private static void checkAndComplete(FeedProvider feedProvider, TorrentInfo torrentInfo) {
		
		// move and remove torrent
		try {
			TorrentStatus torrentStatus = getTorrentStatus(torrentInfo);
			// check if torrent is completed
			if (torrentStatus != null && (torrentStatus.getStatus() == StatusField.seeding || torrentStatus.getStatus() == StatusField.seedWait)) {
				log.log(Level.INFO, "[DEBUG] MediaManager.checkAndComplete Torrent download completed for: " + torrentInfo);
				
				MediaInfo mediaInfo = new MediaInfo(feedProvider, torrentInfo);
				
				String downloadDir = constructAndCreateDownloadDirectory(feedProvider, mediaInfo);
				//downloadDir = "M:/Video/TestDir/"; // TESTING

				//if (feedProvider.getFeedInfo().getDownloadDirectory() != null && !feedProvider.getFeedInfo().getDownloadDirectory().isEmpty()) {
				if (downloadDir != null && !downloadDir.isEmpty()) {
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
						for (int i = 0; i < fileArray.length(); i++) { // && !unrared ?
							// need a way to avoid extracting the same rar, if multipart rar files all end with .rar
							JSONObject obj = new JSONObject(fileArray.get(i).toString());
							String filename = obj.getString("name");
							if (filename.endsWith(".rar")) {
								System.out.println("[DEBUG] found rar file to extract: " + torrentDownloadDir + filename);
								log.log(Level.INFO, "[DEBUG] MediaManager.checkAndComplete unraring archive to: " + downloadDir);
								
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
							log.log(Level.INFO, "[DEBUG] MediaManager.checkAndComplete moving torrent to: " + downloadDir);
							torrentClient.moveTorrents(new Object[] {torrentStatus.getId()}, downloadDir, true);
							// need to set group writable on any folders that are moved
						} else {
							// copy torrent files to downloadDir
							log.log(Level.INFO, "[DEBUG] MediaManager.checkAndComplete copying torrent files to: " + downloadDir);
							
							System.out.println("[DEBUG] COPYING FILES...");
							System.out.println("[DEBUG] downloadDir: " + downloadDir);
							System.out.println("[DEBUG] torrentDownloadDir: " + torrentDownloadDir);
							for (int i = 0; i < fileArray.length(); i++) {
								JSONObject obj = new JSONObject(fileArray.get(i).toString());
								String filename = obj.getString("name");
								System.out.println("[DEBUG] filename: " + filename);
								/*
								 * eg
								 * torrentDownloadDir: /data/?
								 * filename: /?
								 * downloadDir: /data/virtual/TV 
								 */
								// should be creating a hard link if supported by OS, but not possible across file systems (eg pooled file system; mhddfs)
								FileUtils.copyFileToDirectory(new File(torrentDownloadDir + filename), new File(downloadDir)); // overwrites existing files
								//Files.copy(in, target, options); OR Files.setPosixFilePermissions(Paths.get(downloadDir + filename?), perms);
								// http://www.journaldev.com/855/how-to-set-file-permissions-in-java-easily-using-java-7-posixfilepermission
							}
							System.out.println("[DEBUG] ...DONE");
						}
					}
				}
				
				
				
				// checkRemoveRules() // do torrentClient.removeTorrents in this method..., or logic block
				// seed ratios, activity dates, minimum seed time, etc...
				if (feedProvider.getFeedInfo().getRemoveTorrentOnComplete()) {
					log.log(Level.INFO, "[DEBUG] MediaManager.checkAndComplete removing torrent from torrent client");
					torrentClient.removeTorrents(new Object[] {torrentStatus.getId()}, false); // DON'T DELETE TORRENT DATA
				}
				
				
				
				torrentInfo.setStatus(TorrentInfo.STATUS_NOTIFY_COMPLETED);
				torrentInfo.setDateCompleted(new Date());
				
				notifyComplete(feedProvider, torrentInfo, mediaInfo);
			}
		} catch (IOException | JSONException | RarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			
			
			// this is an error that needs to be reported to the user
			
			
			//notifyError // manual cleanup may be required
		}
	}
	
	private static String constructAndCreateDownloadDirectory(FeedProvider feedProvider, MediaInfo mediaInfo) throws IOException {
		String baseDir = constructDownloadDirectory(feedProvider, mediaInfo);
		
		// create destinationDir if it doesn't exist
		File saveDir = new File(baseDir);
		if(!saveDir.exists()) {
			System.out.println("[DEBUG] baseDir does not exist, creating it: " + baseDir);
			boolean createdDir  = saveDir.mkdirs();
			if (!createdDir) {
				throw new IOException("Unable to create directory: " + saveDir);
			} else {
				System.out.println("[DEBUG] setting writable permissions on created baseDir: " + saveDir);
				saveDir.setWritable(true, false); // note: this sets the directory writable for everyone
			}
		}
		
		return baseDir;
	}
	
	public static String constructDownloadDirectory(FeedProvider feedProvider, MediaInfo mediaInfo) {
		String baseDir = null;
		
		// default download directory
		if (StringUtils.isNotBlank(feedProvider.getFeedInfo().getDownloadDirectory())) {
			baseDir = feedProvider.getFeedInfo().getDownloadDirectory();
		}
		// HD download directory if HD media - not implemented
		/*if (mediaInfo.hd && feedProvider.getFeedInfo().getDownloadDirectoryHd() != null && !feedProvider.getFeedInfo().getDownloadDirectoryHd().isEmpty()) {
			baseDir = feedProvider.getFeedInfo().getDownloadDirectoryHd();
		}*/
		
		if (baseDir != null && !baseDir.endsWith("/")) {
			baseDir += "/";
		}
		if (baseDir != null && feedProvider.getFeedInfo().getDetermineSubDirectory()) {
			baseDir = baseDir + mediaInfo.subDirectory;
		}
		
		return baseDir;
	}
	
	private static void notifyNew(FeedProvider feedProvider, TorrentInfo torrentInfo, MediaInfo mediaInfo) {
		log.log(Level.INFO, "[DEBUG] MediaManager.notifyNew for new torrent: " + torrentInfo);
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
		feedProvider.saveTorrent(torrentInfo);
	}
	
	private static void notifyComplete(FeedProvider feedProvider, TorrentInfo torrentInfo, MediaInfo mediaInfo) {
		if (feedProvider.getFeedInfo().getNotifyEmail() != null && !feedProvider.getFeedInfo().getNotifyEmail().isEmpty()) {
			log.log(Level.INFO, "[DEBUG] MediaManager.notifyComplete for completed torrent: " + torrentInfo);
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
	
	public static TorrentStatus getTorrentStatus(TorrentInfo torrent) throws IOException, JSONException {
		TorrentStatus foundTorrentStatus = null;
		
		// can't rely on torrent id if torrent removed/added from another client, or if transmission restarts, ids change, so get all torrents...
		List<TorrentStatus> torrentStatuses = torrentClient.getAllTorrents(
		//List<TorrentStatus> torrentStatuses = torrentClient.getTorrents(
		//	new int[] {torrent.getClientTorrentId()}, // unreliable, ids can be inconsistent and change on transmission restart
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
		//System.out.println("[DEBUG] torrentStatuses: " + torrentStatuses);
		
		for (TorrentStatus torrentStatus : torrentStatuses) {
			if (torrentStatus != null && StringUtils.isNotBlank(torrent.getHashString()) && torrent.getHashString().equals(torrentStatus.getField(TorrentStatus.TorrentField.hashString))) {
				foundTorrentStatus = torrentStatus;
				break;
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
	
	public static void extractRar(String filename, String destinationDir) throws RarException, IOException {
		// SPAWN NEW THREAD? maybe not if deleting torrent...
		System.out.println("[DEBUG] extractRar filename: " + filename);
		System.out.println("[DEBUG] extractRar destinationDir: " + destinationDir);
		
		final File rar = new File(filename);
		final File destinationFolder = new File(destinationDir);
		
		// check if multi part, and only extract the first file
		Archive downloadedArchive = new Archive(rar);
		System.out.println("[DEBUG] Checking RAR header...");
		downloadedArchive.getMainHeader().print();
		if (downloadedArchive.getMainHeader().isMultiVolume() && !downloadedArchive.getMainHeader().isFirstVolume()) {
			downloadedArchive.close();
			return;
		}
		downloadedArchive.close();
		
		System.out.println("[DEBUG] EXTRACTING...");
		ExtractArchive extractArchive = new ExtractArchive();
		extractArchive.extractArchive(rar, destinationFolder);
		
		// group write permission on created directory // need to test this - don't think this is needed
		/*File savedDir = new File(destinationDir + filename); // strip ".rar" from filename?
		if (savedDir.exists() && savedDir.isDirectory()) {
			System.out.println("[DEBUG] setting writable permissions savedDir: " + savedDir);
			savedDir.setWritable(true, false); // note: this sets the directory writable for everyone
		}*/
		
		System.out.println("[DEBUG] ...DONE");
	}
	
	
	
	private static void debugMethod(FeedProvider feedProvider) {
		log.log(Level.INFO, "[MYMEDIA] MediaManager.debug: "+ debug + ", skipping torrent checks.");
		
		/*try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		if (!feedProvider.getFeedInfo().getActive()) {
			return;
		}
		if (feedProvider.getFeedInfo().getId() != 17) {
			return;
		}
		
		List<TorrentInfo> torrents = new ArrayList<TorrentInfo>(feedProvider.getTorrents());
		
		int t = 0;
		for (TorrentInfo torrent : torrents) {
			t++;
			if (t == 2) {
				System.out.println("[DEBUG] torrent: " + torrent);
				//notifyNew(feedProvider, torrent, new MediaInfo(feedProvider, torrent));
				//break;
			}
		}
		
	}
}
