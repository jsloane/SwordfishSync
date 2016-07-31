grails.cache.config = {
	cache {
		name 'torrentClient'
	}
	defaults {
		eternal: false
		overflowToDisk: false
		maxElementsInMemory: 100
		timeToLiveSeconds: 120
		timeToIdleSeconds: 0
	}
}
