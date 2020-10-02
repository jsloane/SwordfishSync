package swordfishsync.domain;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Feed {

	@Id
	@GeneratedValue
	Long					id;

	@Version
	Long					version;

	Date					dateCreated;
	
	Date					lastUpdated;

	@NotNull
	@Size(max = 768)
	String					url;
	
	Boolean					initilised = false; // use this to skip existing feed entries when adding feed
	
	Boolean					initialPopulate = false; // don't add all existing torrents when feed first added, by default
	
	Date					lastFetched;
	
	Date					lastPurged;
	
	Integer					ttl = 0;
	
	Boolean					isCurrent = false; // use this to check if an exception occurred (connection timeout, etc) when checking completed torrents to remove
	
	@OneToMany(orphanRemoval = true, mappedBy = "feed")
	Set<Torrent> torrents = new TreeSet<Torrent>();

	@OneToMany(mappedBy = "feed")
	Set<FeedProvider> feedProviders = new TreeSet<FeedProvider>();

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

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getInitilised() {
		return initilised;
	}

	public void setInitilised(Boolean initilised) {
		this.initilised = initilised;
	}

	public Boolean getInitialPopulate() {
		return initialPopulate;
	}

	public void setInitialPopulate(Boolean initialPopulate) {
		this.initialPopulate = initialPopulate;
	}

	public Date getLastFetched() {
		return lastFetched;
	}

	public void setLastFetched(Date lastFetched) {
		this.lastFetched = lastFetched;
	}

	public Date getLastPurged() {
		return lastPurged;
	}

	public void setLastPurged(Date lastPurged) {
		this.lastPurged = lastPurged;
	}

	public Integer getTtl() {
		return ttl;
	}

	public void setTtl(Integer ttl) {
		this.ttl = ttl;
	}

	public Boolean getIsCurrent() {
		return isCurrent;
	}

	public void setIsCurrent(Boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public Set<Torrent> getTorrents() {
		return torrents;
	}

	public void setTorrents(Set<Torrent> torrents) {
		this.torrents = torrents;
	}

	public Set<FeedProvider> getFeedProviders() {
		return feedProviders;
	}

	public void setFeedProviders(Set<FeedProvider> feedProviders) {
		this.feedProviders = feedProviders;
	}
	
}
