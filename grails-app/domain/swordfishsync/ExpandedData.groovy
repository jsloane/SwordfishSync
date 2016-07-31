package swordfishsync

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class ExpandedData {
	
	String	name
	String	value
	
	static belongsTo = Torrent
	
    static constraints = {
		value	nullable: true, maxSize: 1000
    }
	
	static mapping = {
		version	false
	}
	
}
