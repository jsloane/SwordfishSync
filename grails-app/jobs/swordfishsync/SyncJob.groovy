package swordfishsync

class SyncJob {
	
	FeedService feedService
	
    static triggers = {
		simple startDelay: 5000l, repeatInterval: 1000l * 60l * 1l // execute job every 1 minute
    }
	
    def execute() {
		feedService.doSync()
    }
	
}
