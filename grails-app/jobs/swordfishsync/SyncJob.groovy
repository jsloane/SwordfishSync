package swordfishsync

class SyncJob {
	
	FeedService feedService
	
	def concurrent = false
	
    static triggers = {
		simple startDelay: 5000l, repeatInterval: 1000l * 60l * 1l // execute job every 1 minute
    }
	
    def execute() {
		// create new hibernate session instead of using long running quartz hibernate session
		FeedProvider.withNewSession { session ->
			feedService.doSync()
		}
    }
	
}
