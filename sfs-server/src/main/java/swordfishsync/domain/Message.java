package swordfishsync.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Torrent;

@Entity
public class Message {

	public enum Type {
		SUCCESS, INFO, WARNING, ERROR
	}
	
	public enum Category {
		HTTP, TORRENT_CLIENT, SYSTEM, FILE, SYNC
	}
	
	@Id
	@GeneratedValue
	Long			id;

	@Version
	Long version;

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

	@Size(max = 512)
	String			message;
	
	Boolean			reported;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public FeedProvider getFeedProvider() {
		return feedProvider;
	}

	public void setFeedProvider(FeedProvider feedProvider) {
		this.feedProvider = feedProvider;
	}

	public Torrent getTorrent() {
		return torrent;
	}

	public void setTorrent(Torrent torrent) {
		this.torrent = torrent;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getReported() {
		return reported;
	}

	public void setReported(Boolean reported) {
		this.reported = reported;
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", dateCreated=" + dateCreated + ", dateUpdated=" + dateUpdated + ", type=" + type
				+ ", category=" + category + ", message=" + message + ", reported=" + reported + "]";
	}
	
}
