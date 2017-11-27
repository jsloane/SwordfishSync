package swordfishsync.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Torrent;

@Entity
public class TorrentState {

	public enum Status {
		NOT_ADDED, NOTIFIED_NOT_ADDED, IN_PROGRESS, NOTIFY_COMPLETED, COMPLETED, SKIPPED
	}

	@Id
	@GeneratedValue
	Long			id;

	@Version
	Long			version;

	@ManyToOne
	@NotNull
	FeedProvider	feedProvider;

	@ManyToOne
	@NotNull
	Torrent			torrent;
	
	@NotNull
	Status			status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
