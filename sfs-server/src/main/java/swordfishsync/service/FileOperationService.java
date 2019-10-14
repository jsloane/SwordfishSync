package swordfishsync.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.extract.ExtractArchive;

import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Message;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;
import swordfishsync.exceptions.ApplicationException;
import swordfishsync.model.TorrentContent;
import swordfishsync.model.TorrentDetails;
import swordfishsync.util.FileSystemUtils;

@Transactional(readOnly = true)
@Service("fileOperationService")
public class FileOperationService {

    private static final Logger log = LoggerFactory.getLogger(FileOperationService.class);

	@Resource
	MessageService messageService;

	@Resource
	TorrentClientService torrentClientService;

	@Async
	public void processTorrentCompletion(TorrentState torrentState, TorrentDetails torrentDetails, TorrentContent torrentContent) {

		try {
			boolean successfullyCompleted = true;
			boolean movedOrCopiedData = false;
			String downloadDirectory = torrentContent.getDownloadDirectory();
			if (StringUtils.isNotBlank(downloadDirectory)) {
				createDirectory(downloadDirectory, torrentState.getFeedProvider());
				
				String torrentDownloadedToDirectory = torrentDetails.getDownloadedToDirectory();
				if (!torrentDownloadedToDirectory.endsWith(System.getProperty("file.separator"))) {
					torrentDownloadedToDirectory += System.getProperty("file.separator");
				}
				
				if (BooleanUtils.isTrue(torrentState.getFeedProvider().getExtractRars())) {
					for (String filename : torrentDetails.getFiles()) {
						if (filename.endsWith(".rar")) {
							log.info("Found rar file [" + torrentDownloadedToDirectory + filename + "], extracting to [" + downloadDirectory + "]");
							// extract rar file
							extractRar(torrentDownloadedToDirectory + filename, downloadDirectory, torrentState.getFeedProvider()); // overwrites existing files
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
						for (String filename : torrentDetails.getFiles()) {
							File fileToCopy = new File(torrentDownloadedToDirectory + filename);
							File targetFile = new File(downloadDirectory + fileToCopy.getName());
							String targetFileLocation = downloadDirectory + System.getProperty("file.separator") + fileToCopy.getName();
							log.info("Copying file [" + filename + "] to [" + targetFileLocation + "]");

							try {
								// could be creating a hard link if supported by OS, but not possible across file systems (eg pooled file system; mhddfs)
								Files.copy(fileToCopy.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
								FileSystemUtils.setFilePermissions(targetFile, torrentState.getFeedProvider());
								FileSystemUtils.setFilePermissions(
										new File(targetFileLocation), torrentState.getFeedProvider());
								// http://www.journaldev.com/855/how-to-set-file-permissions-in-java-easily-using-java-7-posixfilepermission
							} catch (IOException e) {
								// display error message in UI and notify user
								messageService.logMessage(true, Message.Type.ERROR, Message.Category.FILE_COPY, torrentState.getFeedProvider(), torrentState.getTorrent(),
										"Error copying torrent file. File cleanup may be required. Exception: " + e.toString());
								throw new ApplicationException("Error copying files", e);
							}
						}
						log.info("...Finished copying torrent files");
					}
				}
			}
			
			// todo: check seed ratios, activity dates, minimum seed time, etc... in new method checkRemoveRules()
			if (torrentState.getFeedProvider().getRemoveTorrentDataOnComplete()) {
				boolean deleteData = false; // Don't delete torrent local data by default
				if (movedOrCopiedData) {
					deleteData = true; // Delete torrent local data if option selected, and data moved or copied to the download directory
				}
				log.info("Removing torrent from torrent client");
				torrentClientService.removeTorrent(torrentState.getTorrent(), deleteData);
			}
			
			if (successfullyCompleted) {
				runSystemCommand(torrentState.getFeedProvider(), torrentState.getTorrent(), torrentContent);
			}
			
		} catch (Exception e) {
			log.error("An error occurred completing torrent: " + torrentState.getTorrent().getName(), e);

			// display error message in UI and notify user
			messageService.logMessage(true, Message.Type.ERROR, Message.Category.FILE_OPERATION, torrentState.getFeedProvider(), torrentState.getTorrent(),
					"Error completing torrent. File cleanup may be required. Exception: " + e.toString());
		}
		
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
			try {
				Files.createDirectories(saveDir.toPath());
				log.info("Setting file permissions on created directory: " + downloadDirectory);
				FileSystemUtils.setFilePermissions(saveDir, feedProvider);
			} catch (IOException e) {
				throw new ApplicationException("Unable to create directory: " + downloadDirectory + ". Exception: " + e.toString(), e);
			}
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
	}
	
	public void runSystemCommand(FeedProvider feedProvider, Torrent torrent, TorrentContent torrentContent) {
		if (StringUtils.isNotBlank(feedProvider.getSystemCommand())) {
			File workingDirectory = new File(feedProvider.getSystemCommand()).getParentFile();
			
			List<String> commandAndArguments = new ArrayList<String>();
			commandAndArguments.add(0, feedProvider.getSystemCommand());
			commandAndArguments.add(1, "name=" + torrent.getName());
			if (StringUtils.isNotBlank(torrentContent.getDownloadDirectory())) {
				commandAndArguments.add(2, "directory=" + torrentContent.getDownloadDirectory());
			}
			
			ProcessBuilder pb = new ProcessBuilder(commandAndArguments);
			pb.directory(workingDirectory);
			
			log.info("Executing system command: " + commandAndArguments);
			
			try {
				Process process = pb.start();
				// TODO check if output data exists before logging
				log.info(new String(IOUtils.toByteArray(process.getInputStream())));
				log.error(new String(IOUtils.toByteArray(process.getErrorStream())));
			} catch (IOException e) {
				// log/report error and continue
				log.error("An error occurred executing system command [" + commandAndArguments + "]", e);
				messageService.logMessage(true, Message.Type.ERROR, Message.Category.SYS_CMD, feedProvider, torrent,
						"Error executing system command. Exception: " + e.toString());
			}
		}
	}
	
}
