package swordfishsync

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class TorrentDetails {
	
	public enum Status {
		UNKNOWN, STOPPED, PAUSED, SEEDING, SEEDWAIT, FINISHED, DOWNLOADING, QUEUED
	}
	
	String			name
	Status			status
	String			downloadedToDirectory
	String			hashString
	Double			percentDone
	Date			activityDate
	List<String>	files = []
	
}
