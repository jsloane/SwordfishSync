package swordfishsync

import java.util.Set

import java.util.Date
import java.util.List

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes=['filterAttributes','torrentStates'])
@EqualsAndHashCode(excludes=['filterAttributes','torrentStates'])
class FeedProvider {
	
	public enum FeedFilterAction {
		ADD, IGNORE
	}
	public enum FeedAction {
		DOWNLOAD, NOTIFY, SKIP
	}
	
	Long					id
	Date					dateCreated
	Date					lastUpdated
	
	String					name
	Feed					feed
	Boolean					active = false // default to disabled feed
	String					downloadDirectory
	Boolean					determineSubDirectory = false
	Boolean					extractRars = false
	String					systemCommand // system command to execute when torrent completes
	Integer					syncInterval = 0 // sync interval in minutes, overrides the ttl
	FeedAction				feedAction = FeedAction.SKIP
	Integer					uploadLimit = 0 // in Kbps
	Integer					deleteInterval = 90 // in days
	String					notifyEmail // notify download compelete, and new torrents waiting to download
	String					detailsUrlValueFromRegex // eg http://localhost/(.*)/file.torrent
	String					detailsUrlFormat // eg http://localhost/details?id={regex-value}
	Boolean					skipDuplicates = true
	Boolean					skipPropersRepacksReals = false
	Boolean					removeTorrentOnComplete = false
	Boolean					removeTorrentDataOnComplete = false
	Boolean					filterEnabled = false
	Boolean					removeAddFilterOnMatch = false
	Date					lastProcessed
	FeedFilterAction		filterAction = FeedFilterAction.IGNORE
	FeedFilterAction		filterPrecedence = FeedFilterAction.IGNORE
	Set<FilterAttribute>	filterAttributes = []
	
	static hasMany = [filterAttributes: FilterAttribute, torrentStates: TorrentState]
	
	def getTorrentDetailsUrl(torrent) {
		// todo
	}
	
    static constraints = {
		name						nullable: false
		active						nullable: false
		feed						nullable: false
		feedAction					nullable: false
		lastProcessed				nullable: true, bindable: false, display: false
		notifyEmail					nullable: true
		syncInterval				min: 0
		deleteInterval				min: 0
		uploadLimit					nullable: false
		downloadDirectory			nullable: true
		determineSubDirectory		nullable: false
		skipDuplicates				nullable: false
		skipPropersRepacksReals		nullable: false
		extractRars					nullable: false
		removeTorrentOnComplete		nullable: false
		removeTorrentDataOnComplete	nullable: false
		systemCommand				nullable: true
		detailsUrlValueFromRegex	nullable: true
		detailsUrlFormat			nullable: true
		filterEnabled				nullable: false
		removeAddFilterOnMatch		nullable: false
		filterAction				nullable: false
		filterPrecedence			nullable: false
		filterAttributes			nullable: true, display: false
    }
	
	static mapping = {
		version				false
		filterAttributes	cascade: 'all-delete-orphan'
		torrentStates		cascade: 'all-delete-orphan'
	}
	
}
