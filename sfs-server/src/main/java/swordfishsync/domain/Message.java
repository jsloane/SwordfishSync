package swordfishsync.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Torrent;

@Entity
public class Message {

	public enum Type {
		SUCCESS, INFO, WARNING, DANGER
	}
	
	public enum Category {
		HTTP, TORRENT_CLIENT, SYSTEM, FILE, SYNC
	}
	
	@Id
	@GeneratedValue
	Long			id;

	Date			dateCreated;
	Date			dateUpdated;

	@NotNull
	Type			type;

	@NotNull
	Category		category;

	@ManyToOne
	@NotNull
	FeedProvider	feedProvider;

	@ManyToOne
	@NotNull
	Torrent			torrent;
	
	String			message;
	
	Boolean			reported;
	
}
