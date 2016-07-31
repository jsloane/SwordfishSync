package swordfishsync

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class TorrentContent {
	
	public enum Type {
		DEFAULT, TV, MOVIE
		// todo: MUSIC
	}
	
	private static final String NAME_KEY = "showname"
	private static final String EPISODE_ID_KEY = "episode"
	
	Type type = Type.DEFAULT // tv/movie/default used for email type
	String subDirectory // store directory under the download directory
	String downloadDirectory
	String name
	
	// common
	String posterUrl
	String backdropUrl
	String extraInfo
	String detailsUrl // this variable is used in the notification email (and torrent listing?)
	String notice
	
	// tv
	String episodeId
	String seasonNumber
	String episodeNumber
	String episodeTitle
	String episodeDescription
	Boolean proper = false
	Boolean repack = false
	Boolean real = false
	
	// movie
	String year
	String quality
	
}
