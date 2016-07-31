package swordfishsync

import groovy.transform.EqualsAndHashCode
//import groovy.transform.ToString

//@ToString
@EqualsAndHashCode
class FilterAttribute {
	
	FeedProvider.FeedFilterAction	filterType
	String							filterRegex
	
	static belongsTo = FeedProvider
	
    static constraints = {
		
    }
	
	static mapping = {
		version	false
	}
	
	@Override
	String toString() {
		return (filterRegex ?: '')
	}
	
}
