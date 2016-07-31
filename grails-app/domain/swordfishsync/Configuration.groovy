package swordfishsync

import java.util.Date;

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class Configuration {
	
	String				title
	List<Configuration>	childConfiguration = []
	Setting				setting
	
	static hasMany = [childConfiguration: Configuration]
	
    static constraints = {
		setting	nullable: true
    }
	
	static mapping = {
		version	false
	}
	
}
