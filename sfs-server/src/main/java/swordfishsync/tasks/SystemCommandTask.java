package swordfishsync.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
				pb.start();
                pb.redirectOutput(); // TODO redirect to sfs-server.log
			} catch (IOException e) {
				e.printStackTrace();
				throw new ApplicationException("Error executing system command.", e);
			}
		} catch (Exception e) {
    		log.warn("Exception executing system command. ", e);
			e.printStackTrace();
		}
	}

}
