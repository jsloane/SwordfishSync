package swordfishsync

class Message {
	
	public enum Type {
		SUCCESS, INFO, WARNING, DANGER
	}
	
	public enum Category {
		HTTP, TORRENT_CLIENT, SYSTEM, FILE, SYNC
	}
	
	Long	id
	Date	dateCreated
	Date	dateUpdated
	
	Type			type
	Category		category
	FeedProvider	feedProvider
	Torrent			torrent
	String			message
	Boolean			reported
	
    static constraints = {
		category		nullable: true
		feedProvider	nullable: true
		torrent			nullable: true
		message			nullable: true, maxSize: 1024
		reported		nullable: true
    }
	
	static mapping = {
		//version	false
	}
	
}
