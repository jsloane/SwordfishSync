package mymedia.db.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.IndexColumn;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

@Entity
@Table(name="feed")
public class FeedInfo {

    @XStreamOmitField
	@Id
	@Column(name="id")
	@GeneratedValue
	private Integer id; // Long id;?

	@Column(name="url", length=1024/*, unique=true - errors on hibernate table creation */)
	private String url;
	
	@Column(name="initilised")
	private Boolean initilised = false;
	
	@Column(name="initial_populate")
	private Boolean initialPopulate = false; // don't add all existing torrents when feed first added, by default

	@Column(name="name")
	private String name;
	
	@Column(name="active")
	private Boolean active = true;
	
	@Column(name="extract_rars")
	private Boolean extractRars = false;
	
	@Column(name="determine_sub_directory")
	private Boolean determineSubDirectory = false;

	@Column(name="download_directory")
	private String downloadDirectory;

	@Column(name="sync_interval")
	private Integer syncInterval = 0; // sync interval in minutes, overrides the ttl
	
	@Column(name="action")
	private String action = "download";
	
	@Column(name="upload_limit")
	private Integer uploadLimit = 0; // in Kbps
	
	@Column(name="delete_interval")
	private Integer deleteInterval = 90; // in days
	
	@Column(name="notify_email")
	private String notifyEmail; // notify download compelete, and new torrents waiting to download
	
	@Column(name="remove_torrent_on_complete")
	private Boolean removeTorrentOnComplete = false;
	
	@Column(name="remove_add_filter_on_match")
	private Boolean removeAddFilterOnMatch = false;
	
	@Column(name="enable_filter")
	private Boolean filterEnabled = false;

	@Column(name="filter_action")
	private String filterAction = "ignore"; // add/ignore

	@Column(name="filter_precedence")
	private String filterPrecedence = "ignore"; // add/ignore
	
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="feed_filter", joinColumns=@JoinColumn(name="feed_id"))
    private Set<FilterAttribute> filterAttributes = new HashSet<FilterAttribute>();
	
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created")
	private Date created = new Date();
	
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated")
	private Date updated = new Date();
	
    @XStreamOmitField
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="feed_id")
    @IndexColumn(name="idx")
    private List<TorrentInfo> feedTorrents = new ArrayList<TorrentInfo>(); // this is the list stored in the DB
	
   	public Integer getId() {
   		return id;
   	}
   	public String getUrl() {
   		return url;
   	}
   	public boolean getInitilised() {
   		return initilised;
   	}
   	public boolean getInitialPopulate() {
   		return initialPopulate;
   	}
   	public String getName() {
   		return name;
   	}
   	public String getDownloadDirectory() {
   		return downloadDirectory;
   	}
   	public boolean getActive() {
   		return active;
   	}
   	public boolean getExtractRars() {
   		return extractRars;
   	}
   	public boolean getDetermineSubDirectory() {
   		return determineSubDirectory;
   	}
   	public List<TorrentInfo> getFeedTorrents() {
   		return feedTorrents;
   	}
   	public Set<FilterAttribute> getFilterAttributes() {
   		return filterAttributes;
   	}
	public Integer getSyncInterval() {
		return syncInterval;
	}
	public String getAction() {
		return action;
	}
	public int getUploadLimit() {
		return uploadLimit;
	}
	public int getDeleteInterval() {
		return deleteInterval;
	}
	public String getNotifyEmail() {
		return notifyEmail;
	}
	public boolean getRemoveTorrentOnComplete() {
		return removeTorrentOnComplete;
	}
	public boolean getRemoveAddFilterOnMatch() {
		return removeAddFilterOnMatch;
	}
	public boolean getFilterEnabled() {
		return filterEnabled;
	}
	public String getFilterAction() {
		return filterAction;
	}
	public String getFilterPrecedence() {
		return filterPrecedence;
	}
	public Date getCreated() {
		return created;
	}
	public Date getUpdated() {
		return updated;
	}
   	
   	public void setId(Integer id) {
   		this.id = id;
   	}
   	public void setUrl(String url) {
   		this.url = url;
   	}
   	public void setInitilised(boolean initilised) {
   		this.initilised = initilised;
   	}
   	public void setInitialPopulate(boolean initialPopulate) {
   		this.initialPopulate = initialPopulate;
   	}
   	public void setName(String name) {
   		this.name = name;
   	}
   	public void setDownloadDirectory(String downloadDirectory) {
   		this.downloadDirectory = downloadDirectory;
   	}
   	public void setActive(boolean active) {
   		this.active = active;
   	}
   	public void setExtractRars(boolean extractRars) {
   		this.extractRars = extractRars;
   	}
   	public void setDetermineSubDirectory(boolean determineSubDirectory) {
   		this.determineSubDirectory = determineSubDirectory;
   	}
   	public void setFeedTorrents(List<TorrentInfo> feedTorrents) {
   		this.feedTorrents = feedTorrents;
   	}
   	public void setFilterAttributes(Set<FilterAttribute> filterAttributes) {
   		this.filterAttributes = filterAttributes;
   	}
	public void setSyncInterval(Integer syncInterval) {
		this.syncInterval = syncInterval;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public void setUploadLimit(int uploadLimit) {
		this.uploadLimit = uploadLimit;
	}
	public void setDeleteInterval(int deleteInterval) {
		this.deleteInterval = deleteInterval;
	}
	public void setNotifyEmail(String notifyEmail) {
		this.notifyEmail = notifyEmail;
	}
	public void setRemoveTorrentOnComplete(boolean removeTorrentOnComplete) {
		this.removeTorrentOnComplete = removeTorrentOnComplete;
	}
	public void setRemoveAddFilterOnMatch(boolean removeAddFilterOnMatch) {
		this.removeAddFilterOnMatch = removeAddFilterOnMatch;
	}
	public void setFilterEnabled(boolean filterEnabled) {
		this.filterEnabled = filterEnabled;
	}
	public void setFilterAction(String filterAction) {
		this.filterAction = filterAction;
	}
	public void setFilterPrecedence(String filterPrecedence) {
		this.filterPrecedence = filterPrecedence;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
   	
   	public String toString() {
        return "FeedInfo: id [" + id + "], name [" + name + "], url [" + url + "]";
   	}
}