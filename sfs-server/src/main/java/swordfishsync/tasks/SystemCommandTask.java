package swordfishsync.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import swordfishsync.domain.Message;
import swordfishsync.domain.Torrent;
import swordfishsync.exceptions.ApplicationException;
import swordfishsync.model.TorrentContent;

public class SystemCommandTask implements Runnable {
	
    private static final Logger log = LoggerFactory.getLogger(SystemCommandTask.class);
	
	private String systemCommand;
	private Torrent torrent;
	private TorrentContent torrentContent;
	
	public SystemCommandTask(String systemCommand, Torrent torrent, TorrentContent torrentContent) {
		this.systemCommand = systemCommand;
		this.torrent = torrent;
		this.torrentContent = torrentContent;
	}
	
	@Override
	public void run() {
		try {
			File workingDirectory = new File(systemCommand).getParentFile();
			
			List<String> commandAndArguments = new ArrayList<String>();
			commandAndArguments.add(0, systemCommand);
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
				// log error
				log.error("An error occurred executing system command [" + commandAndArguments + "]", e);
				throw new ApplicationException("Error executing system command.", e);

				// TODO use task with spring dependency injection
				// display error message in UI
				//messageService.logMessage(false?, Message.Type.ERROR, Message.Category.?, null, null,
				//		"An error occurred executing system command. Exception: " + e.toString());
			}
		} catch (Exception e) {
			// log error
    		log.error("Exception executing system command. ", e);
		}
	}

}
