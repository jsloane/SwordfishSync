package mymedia.services.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import mymedia.db.form.TorrentInfo;
import mymedia.exceptions.ApplicationException;

public class SystemCommandTask implements Runnable {
	
	private final static Logger log = Logger.getLogger(SystemCommandTask.class.getName());
	
	private String systemCommand;
	private TorrentInfo torrentInfo;
	
	public SystemCommandTask(String systemCommand, TorrentInfo torrentInfo) {
		this.systemCommand = systemCommand;
		this.torrentInfo = torrentInfo;
	}
	
	@Override
	public void run() {
		try {
			File workingDirectory = new File(systemCommand).getParentFile();
			
			String downloadDirectory = torrentInfo.getDownloadDirectoryLocation();
			List<String> commandAndArguments = new ArrayList<String>();
			commandAndArguments.add(0, systemCommand);
			commandAndArguments.add(1, "name=" + torrentInfo.getName());
			if (StringUtils.isNotBlank(downloadDirectory)) {
				commandAndArguments.add(2, "directory=" + downloadDirectory);
			}
			
			ProcessBuilder pb = new ProcessBuilder(commandAndArguments);
			pb.directory(workingDirectory);
			
			log.log(Level.INFO, "Executing system command: " + commandAndArguments);
			
			try {
				pb.start();
			} catch (IOException e) {
				throw new ApplicationException("Error executing system command.", e);
			}
		} catch (Exception e) {
    		log.log(Level.WARNING, "Exception executing system command. ", e);
			e.printStackTrace();
		}
	}
	
}
