package swordfishsync

class Message {
	
	public enum Type {
		SUCCESS, INFO, WARNING, DANGER
	}
	
	Long	id
	Date	dateCreated
	
	String	message
	String	code
	Type	type
	
    static constraints = {
		code	nullable: true
    }
	
	static mapping = {
		version	false
	}
	
}
