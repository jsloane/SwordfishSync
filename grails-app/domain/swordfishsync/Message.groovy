package swordfishsync

class Message {
	
	public enum Type {
		SUCCESS, INFO, WARNING, DANGER
	}
	
	public enum Category {
		HTTP, TORRENT_CLIENT, SYSTEM
	}
	
	Long	id
	Date	dateCreated
	
	Type		type
	//String		code
	Category	category
	Feed		feed
	Torrent		torrent
	String		message
	
    static constraints = {
		//code		nullable: true
		category	nullable: true
		feed		nullable: true
		torrent		nullable: true
    }
	
	static mapping = {
		version	false
	}
	
}
