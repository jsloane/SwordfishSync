package swordfishsync

class SyncJob {
	
	FeedService feedService
	
    static triggers = {
		simple startDelay: 5000l, repeatInterval: 1000l * 60l * 5l // execute job every 5 minutes
    }
	
    def execute() {
		feedService.doSync()
    }
	
}
