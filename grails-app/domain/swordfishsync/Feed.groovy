package swordfishsync

import java.util.Date

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes=['feedProviders'])
@EqualsAndHashCode(excludes=['feedProviders'])
class Feed {
	
	Long					id
	Date					dateCreated
	Date					lastUpdated
	
	String					url
	Boolean					initilised = false // use this to skip existing feed entries when adding feed
	Boolean					initialPopulate = false // don't add all existing torrents when feed first added, by default
	Date					lastFetched
	Date					lastPurged
	Integer					ttl = 0
	Boolean					isCurrent = false // use this to check if an exception occurred (connection timeout, etc) when checking completed torrents to remove
	String					errorMessage
	
	static hasMany = [torrents: Torrent, feedProviders: FeedProvider]
	
    static constraints = {
		url				unique: true, maxSize: 1000, url: true
		torrents		nullable: true
		lastFetched		nullable: true
		lastPurged		nullable: true
		errorMessage	nullable: true
    }
	
	static mapping = {
		version		false
		torrents	cascade: 'all-delete-orphan'
	}
	
}
