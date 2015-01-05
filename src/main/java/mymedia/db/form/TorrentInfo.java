package mymedia.db.form;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import mymedia.services.MediaManager;
import mymedia.services.model.FeedProvider;
import mymedia.services.model.MediaInfo;
import ca.benow.transmission.model.TorrentStatus;

@Entity
@Table(name="torrent")
public class TorrentInfo {

	public static final String STATUS_NOT_ADDED = "not_added"; // 
	public static final String STATUS_NOTIFIED_NOT_ADDED = "notified_not_added"; // 
	public static final String STATUS_IN_PROGRESS = "in_progress"; // downloading
	public static final String STATUS_NOTIFY_COMPLETED = "notify_completed"; // finished and removed from torrent client and notification to be sent
	public static final String STATUS_COMPLETED = "completed"; // finished and removed from torrent client
	public static final String STATUS_SKIPPED = "skipped"; // finished and removed from torrent client

	@Id
	@Column(name="id")
	@GeneratedValue
	private Integer id; // Long id?

	@Column(name="name")
	private String name;

	@Column(name="url", length=1024)
	private String url;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="date_added")
	private Date dateAdded;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="date_completed")
	private Date dateCompleted;

	@Column(name="status")
	private String status;

	@Column(name="hash_string")
	private String hashString;

	@Column(name="client_id")
	private Integer clientTorrentId; // client (transmission) torrent Id
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="torrent_data", joinColumns=@JoinColumn(name="torrent_id"))
    private Map<String, String> properties = new HashMap<String, String>(); // only stores one property per key
    
	public TorrentInfo() {
	}
	public TorrentInfo(String name, String url, Date dateAdded, Map<String, String> properties, String status) {
		this.name = name;
		this.url = url;
		this.dateAdded = dateAdded;
		this.properties = properties;
		this.status = status;
	}
	
   	public Integer getId() {
   		return id;
   	}
   	public String getName() {
   		return name;
   	}
   	public String getUrl() {
   		return url;
   	}
   	public Date getDateAdded() {
   		return dateAdded;
   	}
   	public Date getDateCompleted() {
   		return dateCompleted;
   	}
   	public String getStatus() {
   		return status;
   	}
   	public String getHashString() {
   		return hashString;
   	}
   	public Integer getClientTorrentId() {
   		return clientTorrentId;
   	}
   	public Map<String, String> getProperties() {
   		return properties;
   	}
   	
   	public void setId(Integer id) {
   		this.id = id;
   	}
   	public void setName(String name) {
   		this.name = name;
   	}
   	public void setUrl(String url) {
   		this.url = url;
   	}
   	public void setDateAdded(Date dateAdded) {
   		this.dateAdded = dateAdded;
   	}
   	public void setDateCompleted(Date dateCompleted) {
   		this.dateCompleted = dateCompleted;
   	}
   	public void setStatus(String status) {
   		this.status = status;
   	}
   	public void setHashString(String hashString) {
   		this.hashString = hashString;
   	}
   	public void setClientTorrentId(Integer clientTorrentId) {
   		this.clientTorrentId = clientTorrentId;
   	}
   	public void setProperties(Map<String, String> properties) {
   		this.properties = properties;
   	}
   	
   	public TorrentStatus getClientFieldsForTorrent() throws IOException {
   		try {
   	   		return MediaManager.getTorrentStatus(this);
   		} catch (Exception ex) {
   			ex.printStackTrace();
   		}
   		return null;
   	}
   	public String getDownloadDirectoryLocation() {
   		FeedProvider feedProvider = getFeedProvider();
   		return MediaManager.constructDownloadDirectory(feedProvider, new MediaInfo(feedProvider, this, false));
   	}
   	public FeedProvider getFeedProvider() {
   		FeedProvider feedProvider = null;
    	for (FeedProvider feed : MediaManager.feedProviders) {
   			if (feed.getFeedInfo().getFeedTorrents().contains(this)) {
    			feedProvider = feed;
    			break;
   			}
    	}
   		return feedProvider;
   	}
   	
   	public String toString() {
        return "TorrentInfo: name [" + name + "], status [" + status + "]";
   	}
}