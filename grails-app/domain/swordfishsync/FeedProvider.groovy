package swordfishsync

import java.util.Set
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

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
	Boolean					filterEnabled = true
	Boolean					removeAddFilterOnMatch = false
	Date					lastProcessed
	FeedFilterAction		filterAction = FeedFilterAction.IGNORE
	FeedFilterAction		filterPrecedence = FeedFilterAction.IGNORE
	
	def getTorrentDetailsUrl(Torrent torrent) {
		if (!torrent) {
			return null
		}
		
		String url = torrent.detailsUrl
		
		// check if feed has custom notification link url defined
		if (this.detailsUrlValueFromRegex && this.detailsUrlFormat) {
			Pattern pUrl = Pattern.compile(this.detailsUrlValueFromRegex)
			if (pUrl && torrent.url) {
				Matcher mUrl = pUrl.matcher(torrent.url)
				if (mUrl.matches()) {
					url = this.detailsUrlFormat.replace("{regex-value}", mUrl.group(1))
				}
			}
		}
		
		return url
	}
	
	static hasMany = [filterAttributes: FilterAttribute, torrentStates: TorrentState]
	
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
		//version				false
		feed				cascade: 'all-delete-orphan'
		filterAttributes	cascade: 'all-delete-orphan', fetch: 'join'
		torrentStates		cascade: 'all-delete-orphan', fetch: 'join'
	}
	
}
