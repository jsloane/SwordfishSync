package mymedia.services.tasks;

import mymedia.services.MediaManager;

public class SyncTask implements Runnable {
	
	@Override
	public void run() {
		try {
			MediaManager.syncTorrents();
		} catch (Exception ex) {
			// log severe error
			System.out.println("[ERROR]");
			ex.printStackTrace();
		}
	}
	
}
