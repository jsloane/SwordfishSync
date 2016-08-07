package swordfishsync

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class TorrentDetails {
	
	public enum Status {
		UNKNOWN, STOPPED, PAUSED, SEEDING, SEEDWAIT, FINISHED
	}
	
	Status			status
	String			downloadedToDirectory
	String			hashString
	Double			percentDone
	List<String>	files = []
	
}
