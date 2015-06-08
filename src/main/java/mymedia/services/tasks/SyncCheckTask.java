package mymedia.services.tasks;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import mymedia.services.MyMediaLifecycle;

public class SyncCheckTask implements Runnable {
	
	private final static Logger log = Logger.getLogger(SyncCheckTask.class.getName());
	
	private long timeoutMinutes;
	
	public SyncCheckTask(long timeoutMinutes) {
		this.timeoutMinutes = timeoutMinutes;
	}
	
	@Override
	public void run() {
		Date currentDate = new Date();
		long duration  = currentDate.getTime() - MyMediaLifecycle.lastSynced.getTime();
		long durationInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		if (durationInMinutes > timeoutMinutes) {
			log.warning("SyncTask last synced " + durationInMinutes + " minutes ago. Attemping to restart task.");
			MyMediaLifecycle.scheduleSyncTask(true);
		}
	}
	
}
