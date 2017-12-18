package swordfishsync.domain;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Torrent {

	@Id
	@GeneratedValue
	Long				id;

	@Version
	Long				version;

	Date				dateAdded;

	@NotNull
	@Size(max = 768)
	String				url;

	@Size(max = 256)
	String				name;
	
	@Size(max = 768)
	String				detailsUrl;
	
	Date				datePublished;
	Date				dateCompleted;

	@Size(max = 512)
	String				hashString;
	
	Integer				clientTorrentId;
	Boolean				inCurrentFeed;
	Boolean				addedToTorrentClient;

	@OneToMany(orphanRemoval = true, mappedBy = "torrent")
	Set<ExpandedData> expandedData = new TreeSet<ExpandedData>();
	
	@OneToMany(orphanRemoval = true, mappedBy = "torrent")
	Set<TorrentState>  torrentStates = new TreeSet<TorrentState>();
	
	@OneToMany(orphanRemoval = true, mappedBy = "torrent")
	Set<Message>  messages = new TreeSet<Message>();
	
    @ManyToOne
    Feed feed;

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

	public Date getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetailsUrl() {
		return detailsUrl;
	}

	public void setDetailsUrl(String detailsUrl) {
		this.detailsUrl = detailsUrl;
	}

	public Date getDatePublished() {
		return datePublished;
	}

	public void setDatePublished(Date datePublished) {
		this.datePublished = datePublished;
	}

	public Date getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public String getHashString() {
		return hashString;
	}

	public void setHashString(String hashString) {
		this.hashString = hashString;
	}

	public Integer getClientTorrentId() {
		return clientTorrentId;
	}

	public void setClientTorrentId(Integer clientTorrentId) {
		this.clientTorrentId = clientTorrentId;
	}

	public Boolean getInCurrentFeed() {
		return inCurrentFeed;
	}

	public void setInCurrentFeed(Boolean inCurrentFeed) {
		this.inCurrentFeed = inCurrentFeed;
	}

	public Boolean getAddedToTorrentClient() {
		return addedToTorrentClient;
	}

	public void setAddedToTorrentClient(Boolean addedToTorrentClient) {
		this.addedToTorrentClient = addedToTorrentClient;
	}

	public Set<ExpandedData> getExpandedData() {
		return expandedData;
	}

	public void setExpandedData(Set<ExpandedData> expandedData) {
		this.expandedData = expandedData;
	}

	public Set<TorrentState> getTorrentStates() {
		return torrentStates;
	}

	public void setTorrentStates(Set<TorrentState> torrentStates) {
		this.torrentStates = torrentStates;
	}

	public Set<Message> getMessages() {
		return messages;
	}

	public void setMessages(Set<Message> messages) {
		this.messages = messages;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

}
