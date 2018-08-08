package swordfishsync.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.net.ssl.SSLContext;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.extract.ExtractArchive;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import swordfishsync.domain.ExpandedData;
import swordfishsync.domain.Feed;
import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.FeedProvider.FilterAction;
import swordfishsync.domain.FilterAttribute;
import swordfishsync.domain.Message;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;
import swordfishsync.domain.TorrentState.Status;
import swordfishsync.exceptions.ApplicationException;
import swordfishsync.exceptions.TorrentClientException;
import swordfishsync.model.TorrentContent;
import swordfishsync.model.TorrentDetails;
import swordfishsync.repository.FeedProviderRepository;
import swordfishsync.repository.FeedRepository;
import swordfishsync.repository.TorrentRepository;
import swordfishsync.repository.TorrentStateRepository;
import swordfishsync.tasks.SystemCommandTask;
import swordfishsync.util.FileSystemUtils;

@Transactional
@Service("syncService")
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

	@Resource
	FeedProviderRepository feedProviderRepository;
	
	@Resource
	FeedRepository feedRepository;

	@Resource
	TorrentRepository torrentRepository;

	@Resource
	TorrentStateRepository torrentStateRepository;

	@Resource
	TorrentClientService torrentClientService;

	@Resource
	ContentLookupService contentLookupService;

	@Resource
	NotificationService notificationService;
	
	@Resource
	MessageService messageService;

	@Resource
	SettingService settingService;
	
	@Resource
	TaskExecutor taskExecutor;

    //@Transactional
	public void syncFeeds() {
		boolean processedFeeds = false;

		List<FeedProvider> activeFeedProviders = feedProviderRepository.findAllByActive(true);

		for (FeedProvider activeFeedProvider : activeFeedProviders) {
			try {
				syncFeedProvider(activeFeedProvider);
				processedFeeds = true;
			} catch (Exception e) {
				processedFeeds = false;
				log.error("An error occurred syncing feed: " + activeFeedProvider.getName(), e);
			}
			
		}

		if (processedFeeds) {
			// delete feed torrents with no torrent states.
			torrentRepository.deleteAllWithEmptyTorrentStates();
		}

		log.info("Finished syncing feed(s)");
	}
	
	////@Transactional
	public void syncFeedProvider(FeedProvider feedProvider) {
		log.info(String.format("Syncing feed [%s]", feedProvider.getName()));

		Feed feed = feedProvider.getFeed();

		getTorrentsFromFeedSourceAndUpdate(feedProvider, feed);

		List<TorrentState.Status> finishedTorrentStatus = Arrays.asList( // new ArrayList<>(
			TorrentState.Status.NOTIFIED_NOT_ADDED,
			TorrentState.Status.SKIPPED,
			TorrentState.Status.COMPLETED
		);

		List<TorrentState> torrentStatesToProcess = getFeedProviderTorrentStatesByStatuses(feedProvider, finishedTorrentStatus, false);

		for (TorrentState torrentStateToProcess : torrentStatesToProcess) {
			processTorrentState(feedProvider, torrentStateToProcess);
		}

		purgeFeedTorrents(feedProvider, feed, finishedTorrentStatus);


		//feedProvider = feedProviderRepository.findOne(feedProvider.getId());
		feedProvider.setLastProcessed(new Date());
		feedProvider = feedProviderRepository.saveAndFlush(feedProvider);
	}

	//@Transactional
	private void processTorrentState(FeedProvider feedProvider, TorrentState torrentState) {

		try {
			switch (torrentState.getStatus()) {
				case NOT_ADDED: // new torrent, add to torrent client
					checkAndAdd(torrentState);
					break;
				case IN_PROGRESS: // downloading/seeding torrent, check if finished
					checkAndComplete(feedProvider, torrentState);
					break;
				 case NOTIFY_COMPLETED: // retry sending notification that download has completed, should already be done but just in case
					notifyComplete(feedProvider, torrentState, null);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			log.error("An error occurred checking torrent: " + torrentState.getTorrent().getName(), e);

			// display error message in UI
			messageService.logMessage(false, Message.Type.ERROR, Message.Category.SYNC, torrentState.getFeedProvider(), torrentState.getTorrent(),
					String.format("Error processing torrent. Exception: [%s]", e.toString()));
		}

	}

	private void checkAndAdd(TorrentState torrentState) {
		if (shouldAddTorrent(torrentState) && !FeedProvider.Action.SKIP.equals(torrentState.getFeedProvider().getAction())) {
			TorrentContent torrentContent = contentLookupService.getTorrentContentInfo(torrentState.getFeedProvider(), torrentState.getTorrent());
			if (!findDuplicateTorrent(torrentState.getFeedProvider(), torrentState.getTorrent(), torrentContent)) {
				if (FeedProvider.Action.DOWNLOAD.equals(torrentState.getFeedProvider().getAction())) {
					// download
					addTorrent(torrentState);
				} else if (FeedProvider.Action.NOTIFY.equals(torrentState.getFeedProvider().getAction())) {
					// notify only
					try {
						notificationService.sendNotification(torrentState.getFeedProvider(), torrentState.getTorrent(), torrentContent, NotificationService.Type.AVAILABLE);
						setTorrentStatus(torrentState.getFeedProvider(), torrentState.getTorrent(), TorrentState.Status.NOTIFIED_NOT_ADDED);
					} catch (ApplicationException e) {
						log.error("Error sending notification for torrent [" + torrentState.getTorrent().getName() + "]", e);
						messageService.logMessage(false, Message.Type.WARNING, Message.Category.SYNC, torrentState.getFeedProvider(), torrentState.getTorrent(),
								String.format("Error sending notification. Exception: [%s]", e.toString()));
					}
				}
			} else {
				// torrent already downloaded, set to skipped
				log.info("Duplicate torrent detected, skipping [" + torrentState.getTorrent().getName() + "].");
				setTorrentStatus(torrentState.getFeedProvider(), torrentState.getTorrent(), TorrentState.Status.SKIPPED);
			}
		} else {
			// not interested in this torrent, set to skipped
			setTorrentStatus(torrentState.getFeedProvider(), torrentState.getTorrent(), TorrentState.Status.SKIPPED);
		}
	}

	//@Transactional
	private void addTorrent(TorrentState torrentState) {
		try {
			torrentClientService.addTorrent(torrentState);
		} catch (TorrentClientException e) {
			log.error("Error adding torrent", e);
			messageService.logMessage(false, Message.Type.ERROR, Message.Category.TORRENT_CLIENT, torrentState.getFeedProvider(), torrentState.getTorrent(),
					String.format("Error adding torrent. Exception: [%s]", e.toString()));
		}
	}

	private boolean findDuplicateTorrent(FeedProvider feedProvider, Torrent torrent, TorrentContent torrentContent) {
		if (!feedProvider.getSkipDuplicates()) {
			// we want to download duplicate torrents
			return false;
		}
		
		//MediaInfo newTorrentMediaInfo = new MediaInfo(feedProvider, torrentInfo, false);
		
		/*println '### got torrentContent:   ' + torrentContent
		println 'torrentContent.type:         ' + torrentContent.type
		println 'torrentContent.name:         ' + torrentContent.name
		println 'torrentContent.subDirectory: ' + torrentContent.subDirectory
		println 'torrentContent.posterUrl:    ' + torrentContent.posterUrl
		println 'torrentContent.backdropUrl:  ' + torrentContent.backdropUrl*/
		
		if (!feedProvider.getSkipPropersRepacksReals() && (torrentContent.getProper() || torrentContent.getRepack() || torrentContent.getReal())) {
			// get proper/repack/real
			log.info(String.format("Proper/repack/real found for torrent [%s]", torrent.getName()));
			return false;
		}
		
		try {
			// check torrents in progress for the feed
			List<TorrentState.Status> inProgressTorrentStatus = Arrays.asList(TorrentState.Status.IN_PROGRESS);
			List<TorrentState> existingTorrentStates = getFeedProviderTorrentStatesByStatuses(feedProvider, inProgressTorrentStatus, true);
			
			for (TorrentState existingTorrentState : existingTorrentStates) {
				TorrentContent existingTorrentContent = contentLookupService.getTorrentContentInfo(
						feedProvider, existingTorrentState.getTorrent(), existingTorrentState.getTorrent().getName(), false
				);
				if (isSameContent(torrentContent, existingTorrentContent)) {
					return true;
				}
			}
			
			if (StringUtils.isNotBlank(torrentContent.getDownloadDirectory())) {
				// find files in sub directory, if tv, check if any match season/episode
				File dir = new File(torrentContent.getDownloadDirectory()); // todo - use nio package
				File[] files = dir.listFiles();
				
				if (files != null) {
					for (File file : files) {
						if (file.isFile()) {
							TorrentContent fileContent = contentLookupService.getTorrentContentInfo(feedProvider, null, file.getName(), false);
							if (isSameContent(torrentContent, fileContent)) {
								return true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(String.format("Error determining duplicate torrent [%s]", torrent.getName()), e);
			messageService.logMessage(true, Message.Type.ERROR, Message.Category.SYNC, feedProvider, torrent,
					String.format("Error determining duplicate torrent [%s], Exception: [%s]", torrent.getName(), e.toString()));
		}
		
		if (!feedProvider.getDetermineSubDirectory()) {
			// unable to determine sub directory to check for duplicate file
			return false;
		}
		
		return false;
	}
	
	private Boolean isSameContent(TorrentContent newTorrentContent, TorrentContent existingTorrentContent) {
		if (TorrentContent.Type.TV.equals(newTorrentContent.getType())) {
			return (newTorrentContent.getType().equals(existingTorrentContent.getType())
					&& newTorrentContent.getName() != null
					&& newTorrentContent.getName().equalsIgnoreCase(existingTorrentContent.getName())
					&& newTorrentContent.getSeasonNumber() != null
					&& newTorrentContent.getSeasonNumber().equals(existingTorrentContent.getSeasonNumber())
					&& newTorrentContent.getEpisodeNumber() != null
					&& newTorrentContent.getEpisodeNumber().equals(existingTorrentContent.getEpisodeNumber()));
		} else if (TorrentContent.Type.MOVIE.equals(newTorrentContent.getType())) {
			return (newTorrentContent.getType().equals(existingTorrentContent.getType())
					&& newTorrentContent.getName() != null
					&& newTorrentContent.getName().equalsIgnoreCase(existingTorrentContent.getName())
					&& newTorrentContent.getYear() != null
					&& newTorrentContent.getYear().equals(existingTorrentContent.getYear()));
		}
		return false;
	}

	//@Transactional
	private boolean shouldAddTorrent(TorrentState torrentState) {
		return (!torrentState.getFeedProvider().getFilterEnabled() || checkFilterMatch(torrentState));
	}

	//@Transactional
	private boolean checkFilterMatch(TorrentState torrentState) {
		String value = torrentState.getTorrent().getName();
		boolean defaultAction = false;
		
		if (FeedProvider.FilterAction.ADD.equals(torrentState.getFeedProvider().getFilterAction())) {
			defaultAction = true;
		}
		
		// property: precedence = ignore
		/*
			filter
			action when no match: add/ignore (ignore) - defaultAction
			filter precedence add/ignore (ignore) - matchFirst
		 */
		
		FeedProvider.FilterAction matchFirst = torrentState.getFeedProvider().getFilterPrecedence();
		FeedProvider.FilterAction matchSecond = null;
		if (matchFirst == null) {
			matchFirst = FeedProvider.FilterAction.IGNORE;
		}
		if (FeedProvider.FilterAction.IGNORE.equals(matchFirst)) {
			matchSecond = FeedProvider.FilterAction.ADD;
		} else if (FeedProvider.FilterAction.ADD.equals(matchFirst)) {
			matchSecond = FeedProvider.FilterAction.IGNORE;
		} else {
			matchFirst = FeedProvider.FilterAction.IGNORE;
			matchSecond = FeedProvider.FilterAction.ADD;
		}
		
		if (checkFilter(torrentState.getFeedProvider(), matchFirst, value)) {
			if (FeedProvider.FilterAction.ADD.equals(matchFirst)) {
				return true;
			} else if (FeedProvider.FilterAction.IGNORE.equals(matchFirst)) {
				return false;
			}
		}
		if (checkFilter(torrentState.getFeedProvider(), matchSecond, value)) {
			if (FeedProvider.FilterAction.ADD.equals(matchSecond)) {
				return true;
			} else if (FeedProvider.FilterAction.IGNORE.equals(matchSecond)) {
				return false;
			}
		}
		
		return defaultAction;
	}

	//@Transactional
	private boolean checkFilter(FeedProvider feedProvider, FilterAction filterType, String value) {
		boolean match = false;
		boolean removeMatchedRegex = false;
		
		// only remove entries from add watchlist
		if (FeedProvider.FilterAction.ADD.equals(filterType)) {
			removeMatchedRegex = feedProvider.getRemoveAddFilterOnMatch();
		}
		
		Set<FilterAttribute> removeList = new HashSet<FilterAttribute>();
		for (FilterAttribute filterAttribute : feedProvider.getFilterAttributes()) {
			if (filterAttribute.getFilterType().equals(filterType) && value != null && value.matches(filterAttribute.getFilterRegex())) {
				match = true;
				if (removeMatchedRegex) {
					// remove matched regex from filter
					// or have it as a property of the regex string/entry, when the ui is done... THIS? (only for the add/ignore filter)
					// TODO
					removeList.add(filterAttribute);
				}
				break;
			}
		}
		
		if (!removeList.isEmpty()) {
			// update watchlist
			for (FilterAttribute filterAttribute : removeList) {
				feedProvider.getFilterAttributes().remove(filterAttribute);
				// TODO remove without needing to save feedprovider? remove from collection then delete record?
			}
			feedProvider = feedProviderRepository.saveAndFlush(feedProvider);
		}
		
		return match;
	}
	
	
	
	private void checkAndComplete(FeedProvider feedProvider, TorrentState torrentState) {
		TorrentDetails torrentDetails = null;
		
		try {
			torrentDetails = torrentClientService.getTorrentDetails(torrentState.getTorrent(), feedProvider.getExtractRars());
		} catch (TorrentClientException e) {
			log.error("An error occurred retrieving torrent details: " + torrentState.getTorrent().getName(), e);
			
			// display error message in UI and notify user
			messageService.logMessage(false, Message.Type.ERROR, Message.Category.TORRENT_CLIENT, torrentState.getFeedProvider(), torrentState.getTorrent(),
					"Error retrieving torrent details. Exception: " + e.toString());
		}
		
		if (torrentDetails == null) {
			return;
		}
		
		if (TorrentDetails.Status.SEEDING.equals(torrentDetails.getStatus()) ||
			TorrentDetails.Status.SEEDWAIT.equals(torrentDetails.getStatus()) ||
			TorrentDetails.Status.FINISHED.equals(torrentDetails.getStatus()))
		{
			log.info("Torrent download completed for [" + torrentState.getTorrent().getName() + "]");
			TorrentContent torrentContent = contentLookupService.getTorrentContentInfo(feedProvider, torrentState.getTorrent());
			
			try {
				boolean successfullyCompleted = true;
				boolean movedOrCopiedData = false;
				String downloadDirectory = torrentContent.getDownloadDirectory();
				if (StringUtils.isNotBlank(downloadDirectory)) {
					createDirectory(downloadDirectory, feedProvider);
					
					String torrentDownloadedToDirectory = torrentDetails.getDownloadedToDirectory();
					if (!torrentDownloadedToDirectory.endsWith(System.getProperty("file.separator"))) {
						torrentDownloadedToDirectory += System.getProperty("file.separator");
					}
					
					if (feedProvider.getExtractRars()) {
						for (String filename : torrentDetails.getFiles()) {
							if (filename.endsWith(".rar")) {
								log.info("Found rar file [" + torrentDownloadedToDirectory + filename + "], extracting to [" + downloadDirectory + "]");
								// extract rar file
								extractRar(torrentDownloadedToDirectory + filename, downloadDirectory, feedProvider); // overwrites existing files
								movedOrCopiedData = true;
							}
						}
					}
					
					// if not a rar archive, just copy the entire torrent
					if (!movedOrCopiedData) {
						movedOrCopiedData = true;
						if (false) {//feedProvider.removeTorrentOnComplete) { // todo: fix this (does not move data, data loss)
							// just move torrent files using transmission
							log.info("Moving torrent to: " + downloadDirectory);
							//torrentClientService.moveTorrent(torrent, downloadDirectory)
						} else {
							// copy torrent files to downloadDir
							log.info("Copying torrent files from [" + torrentDownloadedToDirectory + "] to [" + downloadDirectory + "]...");
							File downloadDirectoryFile = new File(downloadDirectory);
							for (String filename : torrentDetails.getFiles()) {
								File fileToCopy = new File(torrentDownloadedToDirectory + filename);
								String targetFileLocation = downloadDirectory + System.getProperty("file.separator") + fileToCopy.getName();
								log.info("Copying file [" + filename + "] to [" + targetFileLocation + "]");
								/*
								 * eg
								 * torrentDownloadDir: /data/?
								 * filename: /?
								 * downloadDir: /data/virtual/TV
								 */
								try {
									// should be creating a hard link if supported by OS, but not possible across file systems (eg pooled file system; mhddfs)
									FileUtils.copyFileToDirectory(fileToCopy, downloadDirectoryFile); // note - overwrites existing files
									FileSystemUtils.setFilePermissions(downloadDirectoryFile, feedProvider);
									FileSystemUtils.setFilePermissions(
											new File(targetFileLocation), feedProvider);
									// http://www.journaldev.com/855/how-to-set-file-permissions-in-java-easily-using-java-7-posixfilepermission
								} catch (IOException e) {
									// this is an error that needs to be reported to the user
									throw new ApplicationException("Error copying files", e);
								}
							}
							log.info("...Finished copying torrent files");
						}
					}
				}
				
				// todo: check seed ratios, activity dates, minimum seed time, etc... in new method checkRemoveRules()
				if (feedProvider.getRemoveTorrentDataOnComplete()) {
					boolean deleteData = false; // Don't delete torrent local data by default
					if (feedProvider.getRemoveTorrentDataOnComplete() && movedOrCopiedData) {
						deleteData = true; // Delete torrent local data if option selected, and data moved or copied to the download directory
					}
					log.info("Removing torrent from torrent client");
					torrentClientService.removeTorrent(torrentState.getTorrent(), deleteData);
				}
				
				//torrent.status = Torrent.Status.NOTIFY_COMPLETED
				if (successfullyCompleted) {
					setTorrentStatus(feedProvider, torrentState.getTorrent(), TorrentState.Status.NOTIFY_COMPLETED);
					torrentState.getTorrent().setDateCompleted(new Date());

					notifyComplete(feedProvider, torrentState, torrentContent);
					runSystemCommand(feedProvider, torrentState.getTorrent(), torrentContent);
				}
				
			} catch (ApplicationException | TorrentClientException e) {
				log.error("An error occurred completing torrent: " + torrentState.getTorrent().getName(), e);

				// display error message in UI and notify user
				messageService.logMessage(true, Message.Type.ERROR, Message.Category.FILE, torrentState.getFeedProvider(), torrentState.getTorrent(),
						"Error completing torrent. File cleanup may be required. Exception: " + e.toString());
			}
		}
	}
	
	private void runSystemCommand(FeedProvider feedProvider, Torrent torrent, TorrentContent torrentContent) {
		if (StringUtils.isNotBlank(feedProvider.getSystemCommand())) {
			taskExecutor.execute(
				new SystemCommandTask(feedProvider.getSystemCommand(), torrent, torrentContent)
			);
		}
	}

	private void extractRar(String filename, String destinationDirectory, FeedProvider feedProvider) throws ApplicationException {
		try {
			final File rar = new File(filename);
			final File destinationFolder = new File(destinationDirectory);
			
			// check if multi part, and only extract the first file
			Archive downloadedArchive = new Archive(rar);
			downloadedArchive.getMainHeader().print();
			if (downloadedArchive.getMainHeader().isMultiVolume() && !downloadedArchive.getMainHeader().isFirstVolume()) {
				downloadedArchive.close();
				return;
			}
			downloadedArchive.close();
			
			log.info("Extracting rar file...");
			ExtractArchive extractArchive = new ExtractArchive();
			extractArchive.extractArchive(rar, destinationFolder);
			
			// group write permission on extracted to directory and files
			File extractedToDirectory = new File(destinationDirectory);
			if (extractedToDirectory.exists() && extractedToDirectory.isDirectory()) {
				log.info("Setting file permissions on directory: " + extractedToDirectory);
				FileSystemUtils.setFilePermissions(extractedToDirectory, feedProvider);
				if (extractedToDirectory.listFiles() != null) {
					for (File extractedFile : extractedToDirectory.listFiles()) {
						log.info("Setting file permissions on file: " + extractedFile);
						FileSystemUtils.setFilePermissions(extractedFile, feedProvider);
					}
				}
			}
			
			log.info("...Finished extracting rar file");
		} catch (IOException | RarException e) {
			throw new ApplicationException("Error extracting rar file", e);
		}
		// todo finally... close...
	}

	private void createDirectory(String downloadDirectory, FeedProvider feedProvider) throws ApplicationException {
		// create downloadDirectory if it doesn't exist
		File saveDir = new File(downloadDirectory);
		
		// no longer need these comments..?
		//FileUtils.
		//NameFileComparator comparator = new NameFileComparator(IOCase.SENSITIVE);
		// http://www.javacodegeeks.com/2014/10/apache-commons-io-tutorial.html
		// http://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/IOCase.html
		
		if (!saveDir.exists()) {
			log.info("Download directory does not exist, creating it: " + downloadDirectory);
			boolean createdDir = false;
			try {
				createdDir = saveDir.mkdirs(); // TODO use nio package
				log.info("Setting file permissions on created directory: " + downloadDirectory);
				FileSystemUtils.setFilePermissions(saveDir, feedProvider);
			} catch (Exception e) {
				throw new ApplicationException("Unable to create directory: " + downloadDirectory + ". Exception: " + e.toString(), e);
			}
			if (!createdDir) {
				throw new ApplicationException("Unable to create directory: " + downloadDirectory);
			}
		}
	}

	private void notifyComplete(FeedProvider feedProvider, TorrentState torrentState, TorrentContent torrentContent) {
		if (torrentContent == null) {
			torrentContent = contentLookupService.getTorrentContentInfo(feedProvider, torrentState.getTorrent());
		}
		try {
			notificationService.sendNotification(feedProvider, torrentState.getTorrent(), torrentContent, NotificationService.Type.COMPLETED);
			//torrent.status = Torrent.Status.COMPLETED
			setTorrentStatus(feedProvider, torrentState.getTorrent(), TorrentState.Status.COMPLETED);
		} catch (ApplicationException e) {
			log.error("An error occurred sending notification", e);
			// TODO logError(false, Message.Type.WARNING, Message.Category.SYNC, feedProvider, torrent, e.toString())
		}
	}

	@Transactional
	private void purgeFeedTorrents(FeedProvider feedProvider, Feed feed, List<Status> finishedTorrentStatus) {
		Date currentDate = new Date();
		
		if (feed.getLastPurged() == null) {
			feed.setLastPurged(currentDate);
			feed = feedRepository.saveAndFlush(feed);
		}

		// if the feed has not been purged, or last purged more than a day ago.
		if (feed.getIsCurrent() && Duration.between(feed.getLastPurged().toInstant(), Instant.now()).toDays() > 0) {
			log.info(String.format("Purging feed for [%s], last purged [%s]", feedProvider.getName(), feed.getLastPurged()));

			// delete feed provider torrent states for torrents which have finished and added before delete interval.
			List<FeedProvider> feedFeedProviders = feedProviderRepository.findAllByFeed(feedProvider.getFeed());
			for (FeedProvider feedFeedProvider : feedFeedProviders) {
				LocalDate deleteBeforeLocalDate = LocalDate.now().minusDays(feedFeedProvider.getDeleteInterval());
				Date deleteBeforeDate = Date.from(deleteBeforeLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
				
				log.info("Deleting torrent states for feed provider " + feedFeedProvider.getName() + " from database. Deleting when added before: " + deleteBeforeDate);
				
				torrentStateRepository.deleteByFeedProviderAndStatusInAndTorrentDateAddedBeforeAndTorrentInCurrentFeed(
						feedFeedProvider, finishedTorrentStatus, deleteBeforeDate, false
				);
			}

			feed.setLastPurged(currentDate);
			feed = feedRepository.saveAndFlush(feed);
		}
	}

	/*private boolean checkTorrentStateToRemove(FeedProvider feedProvider, TorrentState torrentState, int maxDeleteInterval) {
		if (!feedProvider.getFeed().getIsCurrent() || torrentState.getTorrent().getInCurrentFeed()) {
			return false;
		}
		
		//long addedInterval = TimeUnit.MILLISECONDS.toDays(new Date().getTime() - torrent.getDateAdded().getTime());
		long addedInterval = ChronoUnit.DAYS.between(feedProvider.getFeed().getLastPurged().toInstant(), torrentState.getTorrent().getDateAdded().toInstant());
		Boolean removeTorrentState = true;
		if (!(addedInterval > maxDeleteInterval && maxDeleteInterval > 0)) {
			removeTorrentState = false;
		}
		
		if (removeTorrentState) {
			log.info("Deleting torrent state " + torrentState.getTorrent().getName() + " from database. Date Added: " + torrentState.getTorrent().getDateAdded() + ", " + addedInterval + " days ago.");
		}
		
		return removeTorrentState;
	}*/

	//@Transactional
	private List<TorrentState> getFeedProviderTorrentStatesByStatuses(FeedProvider feedProvider, List<TorrentState.Status> statuses, boolean inStatus) {
		if (inStatus) {
			return torrentStateRepository.findAllByFeedProviderAndStatusIn(feedProvider, statuses);
		} else {
			return torrentStateRepository.findAllByFeedProviderAndStatusNotIn(feedProvider, statuses);
		}
	}
	
	/*@Transactional
	private List<Torrent> getFeedProviderTorrentsByStatusesAndDateAdded(FeedProvider feedProvider, List<TorrentState.Status> statuses, boolean inStatus, Date dateAddedBefore) {
		if (inStatus) {
			return torrentRepository.findAllByTorrentStatesFeedProviderAndTorrentStatesStatusInAndDateAddedBefore(feedProvider, statuses, dateAddedBefore);
		} else {
			return torrentRepository.findAllByTorrentStatesFeedProviderAndTorrentStatesStatusNotInAndDateAddedBefore(feedProvider, statuses, dateAddedBefore);
		}
	}*/
	
	/*@Transactional
	private List<TorrentState> getTorrentStatesByFeedProviderAndStatusesAndDateAdded(FeedProvider feedProvider, List<TorrentState.Status> statuses, boolean inStatus, Date dateAddedBefore) {
		if (inStatus) {
			return torrentStateRepository.findAllByFeedProviderAndStatusInAndTorrentDateAddedBefore(feedProvider, statuses, dateAddedBefore);
		} else {
			return torrentStateRepository.findAllByFeedProviderAndStatusNotInAndTorrentDateAddedBefore(feedProvider, statuses, dateAddedBefore);
		}
	}*/

	//@Transactional // TODO - force new transaction?
	private void getTorrentsFromFeedSourceAndUpdate(FeedProvider feedProvider, Feed feed) {
		
		if (isItTimeToUpdateFeed(feedProvider)) {
			log.info("Fetching feed: " + feedProvider.getName());
			
			feed.setIsCurrent(false);
			
			boolean feedIsCurrent = false;
			
			SyndFeed syndFeed = getFeedXml(feedProvider);

			if (syndFeed != null) {
				String type = syndFeed.getFeedType();
				// if RSS feed, check for TTL
				if (type.contains("rss")) {
					Channel channel = (Channel) syndFeed.originalWireFeed();
					int ttl = channel.getTtl();
					if (feed.getTtl() != ttl) {
						//feedProvider.feed.ttl = ttl
						feed.setTtl(ttl);
					}
				}
				
				/*def torrentsInCurrentFeed = Torrent.findAllByFeedAndInCurrentFeed(feedProvider.feed, true)
				torrentsInCurrentFeed.each { Torrent torrentInCurrentFeed ->
					//println ''
					//println '### setting torrentInCurrentFeed.inCurrentFeed = false'
					//println '###         torrentInCurrentFeed.name: ' + torrentInCurrentFeed.name
					//println '###         torrentInCurrentFeed.id:   ' + torrentInCurrentFeed.id
					//torrentInCurrentFeed.refresh() // refresh to avoid org.hibernate.StaleObjectStateException
					torrentInCurrentFeed.inCurrentFeed = false
					torrentInCurrentFeed = torrentInCurrentFeed.saveAndFlush()
					//torrentInCurrentFeed = torrentInCurrentFeed.merge()
				}*/
				// set all torrents where inCurrentFeed = true to inCurrentFeed = false
				torrentRepository.setInCurrentFeedByInCurrentFeedAndFeed(false, true, feed);
				
				
				
				
				
				for (Iterator<?> i = syndFeed.getEntries().iterator(); i.hasNext();) {
					SyndEntry entry = (SyndEntry) i.next();
					
					Set<ExpandedData> expandedData = new HashSet<ExpandedData>();
					
					// check for extra data
					for (Element element : (List<Element>) entry.getForeignMarkup()) {
						expandedData.add(new ExpandedData(
							element.getName(),
							element.getValue()
						));
					}
					
					Date datePublished = entry.getPublishedDate();
					if (datePublished == null) {
						datePublished = new Date();
					}
					
					TorrentState.Status newTorrentStatus = TorrentState.Status.NOT_ADDED;
					if (!feed.getInitilised()) {
						newTorrentStatus = TorrentState.Status.SKIPPED; // skip existing feed entries when adding feed
					}
					
					String url = entry.getLink();
					String detailsUrl = null;
					for (SyndEnclosure enclosure : entry.getEnclosures()) {
						if (enclosure.getUrl() != null) {
							detailsUrl = url;
							url = enclosure.getUrl();
						}
					}
					
					if (url != null && !url.isEmpty()) {
						// check torrent doesn't already exist, by checking the url
						//Torrent existingTorrent = Torrent.findByFeedAndUrl(feedProvider.feed, url)
						Torrent existingTorrent = torrentRepository.findByFeedAndUrl(feed, url);
						
						if (existingTorrent == null) {
							// new torrent, add it
							Torrent newTorrent = new Torrent();
							newTorrent.setFeed(feed);
							newTorrent.setInCurrentFeed(true);
							newTorrent.setAddedToTorrentClient(false);
							newTorrent.setUrl(url);
							newTorrent.setName(entry.getTitle());
							newTorrent.setDetailsUrl(detailsUrl);
							newTorrent.setDatePublished(datePublished);
							newTorrent.setDateAdded(new Date());
							newTorrent.setExpandedData(expandedData);
							newTorrent = torrentRepository.saveAndFlush(newTorrent);
							
							List<FeedProvider> feedFeedProviders = feedProviderRepository.findAllByFeed(feed);
							for (FeedProvider feedFeedProvider : feedFeedProviders) {
								setTorrentStatus(feedFeedProvider, newTorrent, newTorrentStatus);
							}
							
						} else if (existingTorrent != null && !existingTorrent.getInCurrentFeed()) {
							// torrent already exists, mark as still in current feed
							existingTorrent.setInCurrentFeed(true);
							existingTorrent = torrentRepository.saveAndFlush(existingTorrent);
						}
						
						feedIsCurrent = true;
					} else {
						String message = String.format("Encountered RSS entry without URL: %s", syndFeed.getDescription());
						log.warn(message);
						messageService.logMessage(true, Message.Type.WARNING, Message.Category.RSS, feedProvider, null, message);
					}
				}
			}
			
			feed.setIsCurrent(feedIsCurrent);
			if (feedIsCurrent) {
				feed.setLastFetched(new Date());
				
				if (!feed.getInitilised()) {
					feed.setInitilised(true);
				}
			}

			feed = feedRepository.saveAndFlush(feed);
		}
	}

	//@Transactional
	void setTorrentStatus(FeedProvider feedProvider, Torrent torrent, Status status) { // TODO move to torrent state service setTorrentStateStatus
		//TorrentState torrentState = TorrentState.findByFeedProviderAndTorrent(feedProvider, torrent)
		TorrentState torrentState = torrentStateRepository.findByFeedProviderAndTorrent(feedProvider, torrent);
		if (torrentState == null) {
			torrentState = new TorrentState();
			torrentState.setFeedProvider(feedProvider);
			torrentState.setTorrent(torrent);
			torrentState.setStatus(status);
		} else {
			torrentState.setStatus(status);
		}
		torrentState = torrentStateRepository.saveAndFlush(torrentState);
	}

	//@Transactional
	private boolean isItTimeToUpdateFeed(FeedProvider feedProvider) {
		Boolean refreshFeed = false;
		if (feedProvider.getFeed().getLastFetched() != null && (feedProvider.getSyncInterval() != 0 || feedProvider.getFeed().getTtl() != 0)) {
			Double fetchedInterval = Math.ceil((double)(new Date().getTime() - feedProvider.getFeed().getLastFetched().getTime()) / 1000 / 60); // round up to the nearest minute
			if (feedProvider.getSyncInterval() != 0) {
				if (fetchedInterval >= feedProvider.getSyncInterval()) {
					// if sync interval has passed, refresh feed
					refreshFeed = true;
				}
			} else if (feedProvider.getFeed().getTtl() != 0 && fetchedInterval >= feedProvider.getFeed().getTtl()) {
				// if ttl has passed and sync interval is not specified, refresh feed
				refreshFeed = true;
			}
		} else {
			// refresh feed anyway
			refreshFeed = true;
		}
		
		return refreshFeed;
	}

	private SyndFeed getFeedXml(FeedProvider feedProvider) {
		XmlReader reader = null;
		SyndFeed syndFeed = null;
		HttpGet httpGet = null;
		CloseableHttpResponse httpResponse = null;
		Boolean acceptUntrustedSslCertificate = settingService.getValue(SettingService.CODE_APP_SECURITY_ACCEPTUNTRUSTEDCERTS, Boolean.class);
		
		try {
			CloseableHttpClient httpClient = null;
			if (acceptUntrustedSslCertificate) {
				// accept any SSL certificate
				SSLContext sslContext = SSLContexts.custom().loadTrustMaterial( // TODO - update this
					null, new TrustStrategy() {
						@Override
						public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
							return true;
						}
					}
				).useTLS().build();
				httpClient = HttpClients.custom()
						.setSslcontext(sslContext) // TODO - update this
						.build();
			} else {
				httpClient = HttpClients.custom().build();
			}
			
			// set http configuration
			RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(120000) // possible socket timeout bug https://bugs.openjdk.java.net/browse/JDK-8049846
					.setConnectTimeout(120000)
					.setConnectionRequestTimeout(120000)
					//.setLocalAddress(localAddress)
					.build();
			httpGet = new HttpGet(feedProvider.getFeed().getUrl());
			httpGet.setConfig(requestConfig);
			log.info("Making request, httpGet: " + httpGet);
			httpResponse = httpClient.execute(httpGet);
			log.info("Made request, httpResponse statusLine: " + httpResponse.getStatusLine());
			
			reader = new XmlReader(httpResponse.getEntity().getContent());
			
			SyndFeedInput syndFeedInput = new SyndFeedInput();
			syndFeedInput.setPreserveWireFeed(true);
			syndFeed = syndFeedInput.build(reader);
		} catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IllegalArgumentException | FeedException e) {
			log.warn("Error making http request.", e);
			
			// display error message in UI
			messageService.logMessage(false, Message.Type.ERROR, Message.Category.HTTP, feedProvider, null, e.toString());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error("Error closing XmlReader", e);
				}
			}
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					log.error("Error closing CloseableHttpResponse", e);
				}
			}
			if (httpGet != null) {
				httpGet.releaseConnection();
			}
		}
		
		return syndFeed;
	}
	
}
