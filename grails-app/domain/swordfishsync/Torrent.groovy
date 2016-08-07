package swordfishsync

import java.util.Date

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includes=['name','url','detailsUrl','datePublished'])
@EqualsAndHashCode(includes=['name','url','detailsUrl','datePublished'])
class Torrent {
	
	def torrentClientService
	
	Long				id
	Date				dateCreated
	
	String				url
	String				name
	String				detailsUrl
	Date				datePublished
	Date				dateCompleted
	String				hashString
	Integer				clientTorrentId
	Boolean				inCurrentFeed
	
	Set<ExpandedData> expandedData = []
	
	TorrentDetails getClientDetails() {
		return torrentClientService.getTorrentDetails(this, false)
	}
	
	public TorrentState getTorrentState(FeedProvider feedProvider) {
		return TorrentState.findByFeedProviderAndTorrent(feedProvider, this)
	}
	
	static transients = ['clientDetails']
	
	static hasMany = [expandedData: ExpandedData, torrentStates: TorrentState]
	
	static belongsTo = [feed: Feed]
	
    static constraints = {
		url				maxSize: 1000
		name			nullable: true
		detailsUrl		maxSize: 1000, nullable: true
		datePublished	nullable: true
		dateCompleted	nullable: true
		hashString		nullable: true, maxSize: 1000
		clientTorrentId	nullable: true
		expandedData	nullable: true
    }
	
	static mapping = {
		version			false
		expandedData	cascade: 'all-delete-orphan'
		torrentStates	cascade: 'all-delete-orphan'
	}
}
