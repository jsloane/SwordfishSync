package swordfishsync.tasks;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import swordfishsync.service.SyncService;

@Component
public class ScheduledTasks {

	@Resource
	SyncService syncService;

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	@Scheduled(initialDelay = 5000, fixedDelay = (1000 * 60 * 3L)) // execute job every 3 minutes
	// @Scheduled(initialDelay = 5000, fixedDelay = (3000 * 10L)) // every 10 seconds
	public void syncTask() {
		syncService.syncFeeds();
	}

}
