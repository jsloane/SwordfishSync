package swordfishsync

class Message {
	
	public enum Type {
		SUCCESS, INFO, WARNING, DANGER
	}
	
	public enum Category {
		HTTP, TORRENT_CLIENT, SYSTEM, FILE
	}
	
	Long	id
	Date	dateCreated
	
	Type		type
	Category	category
	Feed		feed
	Torrent		torrent
	String		message
	
    static constraints = {
		category	nullable: true
		feed		nullable: true
		torrent		nullable: true
    }
	
	static mapping = {
		version	false
	}
	
}
