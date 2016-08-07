package swordfishsync

import swordfishsync.exceptions.ApplicationException
import swordfishsync.exceptions.TorrentClientException

import java.io.File
import java.io.IOException
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.Files
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Set
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.SSLContext

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.SSLContexts
import org.jdom.Element
import org.json.JSONObject
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients

import com.github.junrar.Archive
import com.github.junrar.exception.RarException
import com.github.junrar.extract.ExtractArchive
import com.sun.syndication.feed.rss.Channel
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndFeed
import com.sun.syndication.io.FeedException
import com.sun.syndication.io.SyndFeedInput
import com.sun.syndication.io.XmlReader

import grails.transaction.Transactional
import grails.util.GrailsStringUtils
import groovy.util.logging.Slf4j

import static grails.async.Promises.task
import static grails.async.Promises.onComplete
import static grails.async.Promises.onError

/**
 * 
 * todo: if proper/repack, delete prior managed download? - only log for now
 * 
 */
@Transactional
@Slf4j
class FeedService {
	
	ContentLookupService contentLookupService
	TorrentClientService torrentClientService
	NotificationService notificationService
	// todo in controller - on demand: SyncJob.triggerNow()
	
    def doSync() {
		List<FeedProvider> feedProviders = FeedProvider.findAllByActive(true)
		log.info 'Starting sync for ' + feedProviders?.size() + ' feed(s)'
		feedProviders.each { feedProvider ->
			log.info 'Syncing feed: ' + feedProvider.name
			try {
				syncFeedProvider(feedProvider)
			} catch (Exception e) {
				// todo: report error
				log.error('An error occurred syncing feed: ' + feedProvider.name, e)
			}
		}
		log.info 'Finished syncing feed(s)'
    }
	
	def syncFeedProvider(FeedProvider feedProvider) {
		getTorrentsFromFeedSourceAndUpdate(feedProvider)
		
		def finishedTorrentStatus = [
			TorrentState.Status.NOTIFIED_NOT_ADDED,
			TorrentState.Status.SKIPPED,
			TorrentState.Status.COMPLETED
		]
		
		def torrentsToCheck = getFeedTorrentsByStatuses(feedProvider, finishedTorrentStatus, false)
		//println 'torrentsToCheck: ' + torrentsToCheck
		torrentsToCheck.each { torrent ->
			try {
				checkTorrent(feedProvider, torrent)
			} catch (Exception e) {
				// todo: report error
				log.error('An error occurred checking torrent: ' + torrent.name, e)
			}
		}
		
		if (!feedProvider.feed.lastPurged || feedProvider.feed.lastPurged < (new Date() - 1)) {
			// check torrents to purge
			def torrentIdsToPurge = []
			def torrentsToCheckToPurge = getFeedTorrentsByStatuses(feedProvider, finishedTorrentStatus, true)
			torrentsToCheckToPurge.each { torrent ->
				try {
					Long torrentToPurgeId = checkTorrentToPurge(feedProvider, torrent)
					if (torrentToPurgeId) {
						torrentIdsToPurge.add(torrentToPurgeId)
					}
				} catch (Exception e) {
					// todo: report error
					log.error('An error occurred checking torrent for purging: ' + torrent.name, e)
				}
			}
			
			log.info 'Purging ' + torrentIdsToPurge.size() + ' torrent(s)'
			Torrent.where {
				id in torrentIdsToPurge
			}.deleteAll()
			//feedProvider.feed.torrents.removeAll(torrentsToPurge)
			feedProvider.feed.lastPurged = new Date()
		}
		
		feedProvider.lastProcessed = new Date()
		
		if (feedProvider.isDirty()) {
			feedProvider.save(flush: true)
		}
	}
	
	def deleteFeedProvider(FeedProvider feedProvider) {
		// if feed has no other feed providers
		def otherFeedProviders = FeedProvider.findAllByFeedAndNotId(feedProvider.feed, feedProvider.id)
		
		
	}
	
	Long checkTorrentToPurge(FeedProvider feedProvider, Torrent torrent) {
		Boolean purgeTorrent = checkToRemove(feedProvider, torrent)
		if (purgeTorrent) {
			return torrent.id
		}
		return null
	}
	
	def checkTorrent(FeedProvider feedProvider, Torrent torrent) {
		//println ''
		//println '### check torrent ###'
		//println 'torrent.name:   ' + torrent.name
		TorrentState.Status torrentStatus = getTorrentStatus(feedProvider, torrent)
		//println 'torrentStatus: ' + torrentStatus
		switch (torrentStatus) {
			case TorrentState.Status.NOT_ADDED: // new torrent, add to torrent client
				checkAndAdd(feedProvider, torrent)
				break;
			case TorrentState.Status.IN_PROGRESS: // downloading/seeding torrent, check if finished
				checkAndComplete(feedProvider, torrent)
				break;
			 case TorrentState.Status.NOTIFY_COMPLETED: // retry sending notification that download has completed, should already be done but just in case
				notifyComplete(feedProvider, torrent, null)
				break;
		}
		if (torrent && torrent.isDirty()) {
			//println 'saving torrent'
			//println 'torrent.hashString: ' + torrent.hashString
			
			torrent.save(flush: true)
		}
	}
	
	private def getFeedTorrentsByStatuses(FeedProvider feedProvider, List<TorrentState.Status> statuses, boolean inStatus) {
		return Torrent.createCriteria().list() {
			torrentStates {
				eq('feedProvider', feedProvider)
				if (inStatus) {
					'in'('status', statuses)
				} else {
					not { 'in'('status', statuses) }
				}
			}
		}
	}
	
	TorrentState.Status getTorrentStatus(FeedProvider feedProvider, Torrent torrent) {
		//return TorrentState.findByFeedProviderAndTorrent(feedProvider, torrent)?.status
		return torrent.getTorrentState(feedProvider)?.status
	}
	
	def setTorrentStatus(FeedProvider feedProvider, Torrent torrent, TorrentState.Status status) {
		TorrentState torrentState = TorrentState.findByFeedProviderAndTorrent(feedProvider, torrent)
		if (!torrentState) {
			torrentState = new TorrentState(
				feedProvider: feedProvider,
				torrent: torrent,
				status: status
			)
		} else {
			torrentState.status = status
		}
		torrentState.save(flush: true)
	}
	
	def checkAndAdd(FeedProvider feedProvider, Torrent torrent) {
		if (shouldAddTorrent(feedProvider, torrent) && !FeedProvider.FeedAction.SKIP.equals(feedProvider.feedAction)) {
			TorrentContent torrentContent = contentLookupService.getTorrentContentInfo(feedProvider, torrent, torrent.name)
			if (!foundDuplicateTorrent(feedProvider, torrent, torrentContent)) {
				if (FeedProvider.FeedAction.DOWNLOAD.equals(feedProvider.feedAction)) {
					// download
					addTorrent(feedProvider, torrent)
				} else if (FeedProvider.FeedAction.NOTIFY.equals(feedProvider.feedAction)) {
					// notify only
					try {
						notificationService.sendNotification(feedProvider, torrent, torrentContent, NotificationService.Type.AVAILABLE)
						//torrent.status = Torrent.Status.NOTIFIED_NOT_ADDED
						setTorrentStatus(feedProvider, torrent, TorrentState.Status.NOTIFIED_NOT_ADDED)
					} catch (ApplicationException e) {
						// todo: use NotificationException
						log.error('Error sending notification for torrent: ' + torrent.name, e)
					}
				}
			} else {
				// torrent already downloaded, set to skipped
				log.info 'Duplicate torrent detected, not adding.'
				//torrent.status = Torrent.Status.SKIPPED
				setTorrentStatus(feedProvider, torrent, TorrentState.Status.SKIPPED)
			}
		} else {
			// not interested in this torrent, set to skipped
			//torrent.status = Torrent.Status.SKIPPED
			setTorrentStatus(feedProvider, torrent, TorrentState.Status.SKIPPED)
		}
	}
	
	private def checkAndComplete(FeedProvider feedProvider, Torrent torrent) {
		TorrentDetails torrentDetails = torrentClientService.getTorrentDetails(torrent, feedProvider.extractRars)
		if (TorrentDetails.Status.SEEDING.equals(torrentDetails.status) ||
			TorrentDetails.Status.SEEDWAIT.equals(torrentDetails.status) ||
			TorrentDetails.Status.FINISHED.equals(torrentDetails.status))
		{
			log.info 'Torrent download completed for: ' + torrent.name
			TorrentContent torrentContent = contentLookupService.getTorrentContentInfo(feedProvider, torrent, torrent.name)
			
			try {
				Boolean movedOrCopiedData = false
				String downloadDirectory = torrentContent.downloadDirectory
				if (downloadDirectory) {
					createDirectory(downloadDirectory)
					
					String torrentDownloadedToDirectory = torrentDetails.downloadedToDirectory
					if (!torrentDownloadedToDirectory.endsWith(System.getProperty('file.separator'))) {
						torrentDownloadedToDirectory += System.getProperty('file.separator');
					}
					
					if (feedProvider.extractRars) {
						torrentDetails.files.each { filename ->
							if (filename.endsWith('.rar')) {
								log.info 'Found rar file [' + torrentDownloadedToDirectory + filename + '], extracting to [' + downloadDirectory + ']'
								// extract rar file
								extractRar(torrentDownloadedToDirectory + filename, downloadDirectory) // overwrites existing files
								movedOrCopiedData = true
							}
						}
					}
					
					// if not a rar archive, just copy the entire torrent
					if (!movedOrCopiedData) {
						movedOrCopiedData = true
						if (feedProvider.removeTorrentOnComplete) {
							// just move torrent files using transmission
							log.info 'Moving torrent to: ' + downloadDirectory
							torrentClientService.moveTorrent(torrent, downloadDirectory)
						} else {
							// copy torrent files to downloadDir
							log.info 'Copying torrent files... From [' + torrentDownloadedToDirectory + '] to [' + downloadDirectory + ']'
							File downloadDirectoryFile = new File(downloadDirectory)
							torrentDetails.files.each { filename ->
								log.info 'Copying file: ' + filename
								/*
								 * eg
								 * torrentDownloadDir: /data/?
								 * filename: /?
								 * downloadDir: /data/virtual/TV
								 */
								try {
									// should be creating a hard link if supported by OS, but not possible across file systems (eg pooled file system; mhddfs)
									File fileToCopy = new File(torrentDownloadedToDirectory + filename)
									FileUtils.copyFileToDirectory(fileToCopy, downloadDirectoryFile); // note - overwrites existing files
									setFilePermissions(fileToCopy)
									// http://www.journaldev.com/855/how-to-set-file-permissions-in-java-easily-using-java-7-posixfilepermission
								} catch (IOException e) {
									// this is an error that needs to be reported to the user
									// Message errorMessage = new Message()
									//notifyError(message) // manual cleanup may be required
									movedOrCopiedData = false
									throw new ApplicationException('Error copying files', e);
								}
							}
							log.info '...Finished copying torrent files'
						}
					}
				}
				
				// todo: check seed ratios, activity dates, minimum seed time, etc... in new method checkRemoveRules()
				if (feedProvider.removeTorrentOnComplete) {
					Boolean deleteData = false // Don't delete torrent local data by default
					if (feedProvider.removeTorrentDataOnComplete && movedOrCopiedData) {
						deleteData = true // Delete torrent local data if option selected, and data moved or copied to the download directory
					}
					log.info 'Removing torrent from torrent client'
					torrentClientService.removeTorrent(torrent, deleteData)
				}
				
				//torrent.status = Torrent.Status.NOTIFY_COMPLETED
				setTorrentStatus(feedProvider, torrent, TorrentState.Status.NOTIFY_COMPLETED)
				torrent.dateCompleted = new Date()
				
				notifyComplete(feedProvider, torrent, torrentContent)
				runSystemCommand(feedProvider, torrent, torrentContent)
				
			} catch (ApplicationException | TorrentClientException e) {
				log.error('An error occurred completing torrent: ' + torrent.name, e)

				// display error message in UI
				Message fileError = Message.findWhere(
					feed: feedProvider.feed,
					torrent: torrent,
					type: Message.Type.DANGER,
					category: Message.Category.FILE
				)
				if (!fileError) {
					fileError = new Message(
						feed: feedProvider.feed,
						torrent: torrent,
						type: Message.Type.DANGER,
						category: Message.Category.FILE
					)
				}
				fileError.dateCreated = new Date()
				fileError.message = 'Error completing torrent. File cleanup may be required. Exception: ' + e.toString()
				fileError.save()
				
				// todo: this is an error that needs to be reported/notified to the user
				//notifyError(fileError)
			}
		}
	}
	
	private def runSystemCommand(FeedProvider feedProvider, Torrent torrent, TorrentContent torrentContent) {
		if (GrailsStringUtils.isNotBlank(feedProvider.systemCommand)) {
			def systemCommandTask = task {
				def commandAndArguments = []
				commandAndArguments.add(feedProvider.systemCommand)
				commandAndArguments.add('"name=' + torrent.name + '"')
				if (torrentContent.downloadDirectory) {
					commandAndArguments.add('"directory=' + torrentContent.downloadDirectory + '"')
				}
				
				log.info 'Executing system command: ' + commandAndArguments
				def process = commandAndArguments.execute()
				def outputStringBuilder = new StringBuilder()
				def errorStringBuilder = new StringBuilder()
				process.waitForProcessOutput(outputStringBuilder, errorStringBuilder)
				process.waitFor()
				int exitValue = process.exitValue()
				log.info 'System command finished with exit value: [' + exitValue + '] output: [' + output + '], error output: [' + errorStringBuilder + ']'
				if (exitValue != 0) {
					throw new ApplicationException('Error executing system command: ', errorStringBuilder);
				}
			}
			
			systemCommandTask.onError { Throwable t ->
				log.error('An error occurred running system command for torrent: ' + torrent.name, t)
			}
			systemCommandTask.onComplete { List results ->
				log.info 'System command completed for torrent: ' + torrent.name
			}
		}
	} 
	
	private def extractRar(String filename, String destinationDirectory) throws RarException, IOException {
		try {
			final File rar = new File(filename)
			final File destinationFolder = new File(destinationDirectory)
			
			// check if multi part, and only extract the first file
			Archive downloadedArchive = new Archive(rar);
			downloadedArchive.getMainHeader().print();
			if (downloadedArchive.getMainHeader().isMultiVolume() && !downloadedArchive.getMainHeader().isFirstVolume()) {
				downloadedArchive.close()
				return
			}
			downloadedArchive.close()
			
			log.info 'Extracting rar file...'
			ExtractArchive extractArchive = new ExtractArchive();
			extractArchive.extractArchive(rar, destinationFolder);
			
			// group write permission on extracted to directory and files - note this makes the directory/files writable to everyone
			File extractedToDirectory = new File(destinationDirectory)
			if (extractedToDirectory.exists() && extractedToDirectory.isDirectory()) {
				log.info 'Setting writable permissions on extracted directory: ' + extractedToDirectory
				setFilePermissions(extractedToDirectory)
				extractedToDirectory.listFiles()?.each { extractedFile ->
					log.info 'Setting writable permissions on extracted file: ' + extractedFile
					setFilePermissions(extractedFile)
				}
			}
			
			log.info '...Finished extracting rar file'
		} catch (IOException | RarException e) {
			throw new ApplicationException('Error extracting rar file', e);
		}
	}
	
	private def setFilePermissions(File file) {
		// Use PosixFilePermission to set file permissions
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>()
		//add owners permission
		perms.add(PosixFilePermission.OWNER_READ)
		perms.add(PosixFilePermission.OWNER_WRITE)
		perms.add(PosixFilePermission.OWNER_EXECUTE)
		//add group permissions
		perms.add(PosixFilePermission.GROUP_READ)
		perms.add(PosixFilePermission.GROUP_WRITE)
		perms.add(PosixFilePermission.GROUP_EXECUTE)
		//add others permissions
		perms.add(PosixFilePermission.OTHERS_READ)
		
		try {
			Files.setPosixFilePermissions(file.toPath(), perms)
		} catch (IOException) {
			log.error('Error setting file permissions for file: ' + file, e)
			// todo: message error
		}
	}
	
	private def createDirectory(String downloadDirectory) throws ApplicationException {
		// create downloadDirectory if it doesn't exist
		File saveDir = new File(downloadDirectory)
		
		//FileUtils.
		//NameFileComparator comparator = new NameFileComparator(IOCase.SENSITIVE);
		// http://www.javacodegeeks.com/2014/10/apache-commons-io-tutorial.html
		// http://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/IOCase.html
		
		if (!saveDir.exists()) { // appears to be case sensitive (linux) - need to address this so case sensitive duplicate directories are not created and use the existing directory
			log.info 'Download directory does not exist, creating it: ' + downloadDirectory
			boolean createdDir = false
			try {
				createdDir = saveDir.mkdirs()
				log.info 'Setting writable permissions on created directory: ' + downloadDirectory
				setFilePermissions(saveDir)
			} catch (Exception e) {
				throw new ApplicationException('Unable to create directory: ' + downloadDirectory, e)
			}
			if (!createdDir) {
				throw new ApplicationException('Unable to create directory: ' + downloadDirectory)
			}
		}
	}
	
	private Boolean checkToRemove(FeedProvider feedProvider, Torrent torrent) {
		if (!feedProvider.feed.isCurrent || torrent.inCurrentFeed) {
			return false
		}
		
		Date completionOrCreatedDate = torrent.dateCompleted
		if (!completionOrCreatedDate) {
			// the torrent wasn't downloaded, so just use the date it was added
			completionOrCreatedDate = torrent.dateCreated
		}
		
		long completedInterval = TimeUnit.MILLISECONDS.toDays(new Date().getTime() - completionOrCreatedDate.getTime())
		Boolean removeTorrent = true
		List<FeedProvider> feedFeedProviders = FeedProvider.findAllByFeed(feedProvider.feed)
		feedFeedProviders.each { feedFeedProvider ->
			if (!(completedInterval > feedFeedProvider.deleteInterval && feedFeedProvider.deleteInterval > 0)) {
				removeTorrent = false
			}
		}
		
		if (removeTorrent) {
			log.info 'Deleting torrent ' + torrent.name + 'from database. Date Completed/Added: ' + torrent.dateCompleted + ', ' + completedInterval + ' days ago.'
		}
		
		return removeTorrent
	}
	
	private def notifyComplete(FeedProvider feedProvider, Torrent torrent, TorrentContent torrentContent) {
		if (!torrentContent) {
			torrentContent = contentLookupService.getTorrentContentInfo(feedProvider, torrent, torrent.name)
		}
		try {
			notificationService.sendNotification(feedProvider, torrent, torrentContent, NotificationService.Type.COMPLETED)
			//torrent.status = Torrent.Status.COMPLETED
			setTorrentStatus(feedProvider, torrent, TorrentState.Status.COMPLETED)
		} catch (ApplicationException e) {
			// todo: render error
			log.error('An error occurred sending notification', e)
		}
	}
	
	def addTorrent(FeedProvider feedProvider, Torrent torrent) {
		try {
			torrentClientService.addTorrent(feedProvider, torrent)
		} catch (TorrentClientException e) {
			// todo: error message handling
		}
	}
	
	private Boolean foundDuplicateTorrent(FeedProvider feedProvider, Torrent torrent, TorrentContent torrentContent) {
		if (!feedProvider.skipDuplicates) {
			// we want to download duplicate torrents
			return false
		}
		
		//MediaInfo newTorrentMediaInfo = new MediaInfo(feedProvider, torrentInfo, false);
		
		/*println '### got torrentContent:   ' + torrentContent
		println 'torrentContent.type:         ' + torrentContent.type
		println 'torrentContent.name:         ' + torrentContent.name
		println 'torrentContent.subDirectory: ' + torrentContent.subDirectory
		println 'torrentContent.posterUrl:    ' + torrentContent.posterUrl
		println 'torrentContent.backdropUrl:  ' + torrentContent.backdropUrl*/
		
		if (!feedProvider.skipPropersRepacksReals && (torrentContent.proper || torrentContent.repack || torrentContent.real)) {
			// get proper/repack/real
			log.info 'Proper/repack/real found.'
			return false
		}
		
		Boolean foundDuplicate = false
		
		try {
			// check torrents in progress for the feed
			//Set<Torrent> existingTorrents = feedProvider.feed.torrents.findAll { TorrentState.Status.IN_PROGRESS.equals(it.getStatus(feedProvider)) }
			def existingTorrents = getFeedTorrentsByStatuses(feedProvider, [TorrentState.Status.IN_PROGRESS], true)
			
			for (Torrent existingTorrent : existingTorrents) {
				TorrentContent existingTorrentContent = contentLookupService.getTorrentContentInfo(feedProvider, existingTorrent, existingTorrent.name, false)
				if (isSameContent(torrentContent, existingTorrentContent)) {
					return true
				}
			}
			
			if (GrailsStringUtils.isNotBlank(torrentContent.downloadDirectory)) {
				// find files in sub directory, if tv, check if any match season/episode
				File dir = new File(torrentContent.downloadDirectory)
				File[] files = dir.listFiles()
				
				for (File file : files) {
					if (file.isFile()) {
						TorrentContent fileContent = contentLookupService.getTorrentContentInfo(feedProvider, null, file.getName(), false)
						if (isSameContent(torrentContent, fileContent)) {
							return true
						}
					}
				}
			}
		} catch (Exception e) {
			log.error('Error determining duplicate torrent', e)
		}
		
		if (!feedProvider.determineSubDirectory) {
			// unable to determine sub directory to check for duplicate file
			return false
		}
		
		return false
	}
	
	private Boolean isSameContent(TorrentContent newTorrentContent, TorrentContent existingTorrentContent) {
		if (TorrentContent.Type.TV.equals(newTorrentContent.type)) {
			return (newTorrentContent.type.equals(existingTorrentContent.type)
					&& newTorrentContent.name != null
					&& newTorrentContent.name.equals(existingTorrentContent.name)
					&& newTorrentContent.seasonNumber != null
					&& newTorrentContent.seasonNumber.equals(existingTorrentContent.seasonNumber)
					&& newTorrentContent.episodeNumber != null
					&& newTorrentContent.episodeNumber.equals(existingTorrentContent.episodeNumber))
		} else if (TorrentContent.Type.MOVIE.equals(newTorrentContent.type)) {
			return (newTorrentContent.type.equals(existingTorrentContent.type)
					&& newTorrentContent.name != null
					&& newTorrentContent.name.equals(existingTorrentContent.name)
					&& newTorrentContent.year != null
					&& newTorrentContent.year.equals(existingTorrentContent.year))
		}
		return false
	}
	
	private Boolean shouldAddTorrent(FeedProvider feedProvider, Torrent torrent) {
		return !feedProvider.filterEnabled || checkFilterMatch(feedProvider, torrent);
	}
	
	private Boolean checkFilterMatch(FeedProvider feedProvider, Torrent torrent) {
		String value = torrent.name
		Boolean defaultAction = false
		
		if (FeedProvider.FeedFilterAction.ADD.equals(feedProvider.filterAction)) {
			defaultAction = true
		}
		
		// property: precedence = ignore
		/*
			filter
			action when no match: add/ignore (ignore) - defaultAction
			filter precedence add/ignore (ignore) - matchFirst
		 */
		
		String matchFirst = feedProvider.filterPrecedence
		String matchSecond = null
		if (matchFirst == null) {
			matchFirst = FeedProvider.FeedFilterAction.IGNORE
		}
		if (FeedProvider.FeedFilterAction.IGNORE.equals(matchFirst)) {
			matchSecond = FeedProvider.FeedFilterAction.ADD
		} else if (FeedProvider.FeedFilterAction.ADD.equals(matchFirst)) {
			matchSecond = FeedProvider.FeedFilterAction.IGNORE
		} else {
			matchFirst = FeedProvider.FeedFilterAction.IGNORE
			matchSecond = FeedProvider.FeedFilterAction.ADD
		}
		
		if (checkFilter(feedProvider, matchFirst, value)) {
			if (FeedProvider.FeedFilterAction.ADD.equals(matchFirst)) {
				return true
			} else if (FeedProvider.FeedFilterAction.IGNORE.equals(matchFirst)) {
				return false
			}
		}
		if (checkFilter(feedProvider, matchSecond, value)) {
			if (FeedProvider.FeedFilterAction.ADD.equals(matchSecond)) {
				return true
			} else if (FeedProvider.FeedFilterAction.IGNORE.equals(matchSecond)) {
				return false
			}
		}
		
		return defaultAction
	}
	
	private Boolean checkFilter(FeedProvider feedProvider, FeedProvider.FeedFilterAction filterType, String value) {
		Boolean match = false
		Boolean removeMatchedRegex = false
		
		// only remove entries from add watchlist
		if (FeedProvider.FeedFilterAction.ADD.equals(filterType)) {
			removeMatchedRegex = feedProvider.removeAddFilterOnMatch
		}
		
		Set<FilterAttribute> removeList = new HashSet<FilterAttribute>()
		for (FilterAttribute filterAttribute : feedProvider.filterAttributes) {
			if (filterAttribute.filterType.equals(filterType) && value != null && value.matches(filterAttribute.filterRegex)) {
				match = true
				if (removeMatchedRegex) {
					// remove matched regex from filter
					// or have it as a property of the regex string/entry, when the ui is done... THIS? (only for the add/ignore filter)
					// todo
					removeList.add(filterAttribute)
				}
				break
			}
		}
		
		if (!removeList.isEmpty()) {
			// update watchlist
			for (FilterAttribute filterAttribute : removeList) {
				feedProvider.filterAttributes.remove(filterAttribute);
			}
			//feedProvider = feedProvider.merge() //?
			//feedProvider.save(flush: true)
		}
		
		return match
	}
	
	private void getTorrentsFromFeedSourceAndUpdate(FeedProvider feedProvider) {
		log.info 'Fetching feed: ' + feedProvider.name
		
		if (isItTimeToUpdateFeed(feedProvider)) {
			feedProvider.feed.isCurrent = false
			
			def torrentsInCurrentFeed = Torrent.findAllByFeedAndInCurrentFeed(feedProvider.feed, true)
			torrentsInCurrentFeed.each { torrentInCurrentFeed ->
				/*println ''
				println '### setting torrentInCurrentFeed.inCurrentFeed = false'
				println '###         torrentInCurrentFeed.name: ' + torrentInCurrentFeed.name
				println '###         torrentInCurrentFeed.id:   ' + torrentInCurrentFeed.id*/
				torrentInCurrentFeed.inCurrentFeed = false
				torrentInCurrentFeed.save(flush: true)
			}
			
			SyndFeed syndFeed = getFeedXml(feedProvider)
			
			if (syndFeed) {
				String type = syndFeed.getFeedType();
				// if RSS feed, check for TTL
				if (type.contains("rss")) {
					Channel channel = (Channel) syndFeed.originalWireFeed();
					int ttl = channel.getTtl()
					if (feedProvider.feed.ttl != ttl) {
						feedProvider.feed.ttl = ttl
					}
				}
				
				for (Iterator<?> i = syndFeed.getEntries().iterator(); i.hasNext();) {
					SyndEntry entry = (SyndEntry) i.next();
					
					Set<ExpandedData> expandedData = new HashSet<ExpandedData>();
					
					// check for extra data
					for (Element element : (List<Element>) entry.getForeignMarkup()) {
						expandedData.add(new ExpandedData(
							name: element.getName(),
							value: element.getValue()
						));
					}
					
					Date datePublished = entry.getPublishedDate()
					if (datePublished == null) {
						datePublished = new Date()
					}
					
					TorrentState.Status torrentStatus = TorrentState.Status.NOT_ADDED
					if (!feedProvider.feed.initilised) {
						torrentStatus = TorrentState.Status.SKIPPED // skip existing feed entries when adding feed
					}
					
					String url = entry.getLink()
					String detailsUrl = null
					entry.getEnclosures().each { enclosure ->
						if (enclosure.url) {
							detailsUrl = url
							url = enclosure.url
						}
					}
					
					// check torrent doesn't already exist, by checking the url
					//Torrent existingTorrent = feedProvider.feed.torrents.find { it.url.equals(url) }
					Torrent existingTorrent = Torrent.findByFeedAndUrl(feedProvider.feed, url)
					
					if (!existingTorrent) {
						// new torrent, add it
						Torrent newTorrent = new Torrent(
							feed:			feedProvider.feed,
							inCurrentFeed:	true,
							url:			url,
							//status:			torrentStatus,
							name:			entry.getTitle(),
							detailsUrl:		detailsUrl,
							datePublished:	datePublished,
							expandedData:	expandedData
						).save(flush: true, failOnError: true)
						//println 'newTorrent: ' + newTorrent
						setTorrentStatus(feedProvider, newTorrent, torrentStatus)
						//feedProvider.feed.addToTorrents(newTorrent)
						feedProvider.save(flush: true)
						/*println ''
						println '### NEW TORRENT'
						println '###         newTorrent.name: ' + newTorrent.name
						println '###         newTorrent.id: ' + newTorrent.id*/
					} else if (existingTorrent && !existingTorrent.inCurrentFeed) {
						// torrent already exists, mark as still in current feed
						/*println ''
						println '### setting existingTorrent.inCurrentFeed = true'
						println '###         existingTorrent.name: ' + existingTorrent.name
						println '###         existingTorrent.id: ' + existingTorrent.id*/
						existingTorrent.inCurrentFeed = true
						existingTorrent.save(flush: true)
					}
					
					feedProvider.feed.isCurrent = true
					feedProvider.feed.lastFetched = new Date()
				}
				
				if (!feedProvider.feed.initilised) {
					feedProvider.feed.initilised = true
				}
			}
			
			feedProvider.save(flush: true)
		}
	}
	
	private Boolean isItTimeToUpdateFeed(FeedProvider feedProvider) {
		Boolean refreshFeed = false
		if (feedProvider.syncInterval != 0 || feedProvider.feed.ttl != 0) {
			Double fetchedInterval = Math.ceil((double)(new Date().getTime() - feedProvider.feed.lastFetched.getTime()) / 1000 / 60); // round up to the nearest minute
			if (feedProvider.syncInterval != 0) {
				if (fetchedInterval >= feedProvider.syncInterval) {
					// if sync interval has passed, refresh feed
					refreshFeed = true
				}
			} else if (feedProvider.feed.ttl != 0 && fetchedInterval >= feedProvider.feed.ttl) {
				// if ttl has passed and sync interval is not specified, refresh feed
				refreshFeed = true
			}
		} else {
			// refresh feed anyway
			refreshFeed = true
		}
		
		return refreshFeed
	}
	
	private SyndFeed getFeedXml(FeedProvider feedProvider) {
		XmlReader reader = null
		SyndFeed syndFeed = null
		HttpGet httpGet = null
		CloseableHttpResponse httpResponse = null
		Boolean acceptUntrustedSslCertificate = true // todo: get from settings
		
		try {
			CloseableHttpClient httpClient = null
			if (acceptUntrustedSslCertificate) {
				// accept any SSL certificate
				SSLContext sslContext = SSLContexts.custom().loadTrustMaterial( // maybe use groovy http client
					null, new TrustStrategy() {
						@Override
						public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
							return true
						}
					}
				).useTLS().build()
				httpClient = HttpClients.custom()
						.setSslcontext(sslContext)
						.build()
			} else {
				httpClient = HttpClients.custom().build()
			}
			
			// set http configuration
			RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(120000) // possible socket timeout bug https://bugs.openjdk.java.net/browse/JDK-8049846
					.setConnectTimeout(120000)
					.setConnectionRequestTimeout(120000)
					//.setLocalAddress(localAddress)
					.build()
			httpGet = new HttpGet(feedProvider.feed.url)
			httpGet.setConfig(requestConfig)
			log.info 'Making request, httpGet: ' + httpGet
			httpResponse = httpClient.execute(httpGet)
			log.info 'Made request, httpResponse statusLine: ' + httpResponse.getStatusLine()
			
			reader = new XmlReader(httpResponse.getEntity().getContent())
			
			SyndFeedInput syndFeedInput = new SyndFeedInput()
			syndFeedInput.setPreserveWireFeed(true)
			syndFeed = syndFeedInput.build(reader)
		} catch (IllegalStateException | IOException | IllegalArgumentException | FeedException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException  e) {
			log.warn('Error making http request.', e)
			
			// display error message in UI
			Message httpError = Message.findWhere(
				feed: feedProvider.feed,
				type: Message.Type.DANGER,
				category: Message.Category.HTTP
			)
			if (!httpError) {
				httpError = new Message(
					feed: feedProvider.feed,
					type: Message.Type.DANGER,
					category: Message.Category.HTTP
				)
			}
			httpError.dateCreated = new Date()
			httpError.message = e.toString()
			httpError.save()
		} finally {
			if (reader != null) {
				try {
					reader.close()
				} catch (IOException e) {
					e.printStackTrace()
				}
			}
			if (httpResponse != null) {
				try {
					httpResponse.close()
				} catch (IOException e) {
					e.printStackTrace()
				}
			}
			if (httpGet != null) {
				httpGet.releaseConnection()
			}
		}
		
		return syndFeed
	}
	
}
