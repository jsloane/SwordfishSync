package swordfishsync

import java.util.Date;

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes=['feed','torrent'])
@EqualsAndHashCode(excludes=['feed','torrent'])
class TorrentState {
	
	public enum Status {
		NOT_ADDED, NOTIFIED_NOT_ADDED, IN_PROGRESS, NOTIFY_COMPLETED, COMPLETED, SKIPPED
	}
	
	Long			id
	
	FeedProvider	feedProvider
	Torrent			torrent
	Status			status
	
    static constraints = {
    }
	
	static mapping = {
		//version	false
	}
	
}
