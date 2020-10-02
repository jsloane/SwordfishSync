package swordfishsync.domain;

import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.ToString;
import swordfishsync.domain.Feed;

@Entity
//@Data
//@EqualsAndHashCode(exclude = {"feed", "filterAttributes", "torrentStates", "messages"})
//@ToString(exclude = {"feed", "filterAttributes", "torrentStates", "messages"})
public class FeedProvider {

	public enum FilterAction {
		ADD, IGNORE
	}
	public enum Action {
		DOWNLOAD, NOTIFY, SKIP
	}

	@Id
	@GeneratedValue
	Long					id;

	@Version
	Long					version;
	
	Date					dateCreated;
	
	Date					lastProcessed;

	@NotNull
	//@Size(min = 1, max = 64)
	@Size(max = 64)
	String					name;

	@ManyToOne // todo - programmatically delete feed if no feed providers
	@NotNull
	Feed					feed;

	@NotNull
	Boolean					active; // default to disabled feed

	@Size(max = 256)
	String					downloadDirectory;

	@NotNull
	Boolean					determineSubDirectory;

	@NotNull
	Boolean					extractRars;

	@Size(max = 256)
	String					systemCommand; // system command to execute when torrent completes

	@NotNull
	Integer					syncInterval; // sync interval in minutes, overrides the ttl

	@NotNull
	Action					action;

	@NotNull
	Integer					uploadLimit; // in Kbps

	@NotNull
	Integer					deleteInterval; // in days

	@Size(max = 128)
	String					notifyEmail; // notify download compelete, and new torrents waiting to download

	@Size(max = 768)
	String					detailsUrlValueFromRegex; // eg http://localhost/(.*)/file.torrent

	@Size(max = 768)
	String					detailsUrlFormat; // eg http://localhost/details?id={regex-value}

	@NotNull
	Boolean					skipDuplicates;

	@NotNull
	Boolean					skipPropersRepacksReals;

	Boolean					propersRepacksReplaceFile;

	@NotNull
	Boolean					removeTorrentOnComplete;

	@NotNull
	Boolean					removeTorrentDataOnComplete;

	@NotNull
	Boolean					filterEnabled;

	@NotNull
	Boolean					removeAddFilterOnMatch;

	@NotNull
	FilterAction			filterAction;

	@NotNull
	FilterAction			filterPrecedence;
	
	@OneToMany(orphanRemoval = true, mappedBy = "feedProvider")
	Set<FilterAttribute>	filterAttributes;

	@OneToMany(orphanRemoval = true, mappedBy = "feedProvider")
	Set<TorrentState>		torrentStates;

	@OneToMany(orphanRemoval = true, mappedBy = "feedProvider")
	Set<Message>  			messages;

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

	public Date getLastProcessed() {
		return lastProcessed;
	}

	public void setLastProcessed(Date lastProcessed) {
		this.lastProcessed = lastProcessed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getDownloadDirectory() {
		return downloadDirectory;
	}

	public void setDownloadDirectory(String downloadDirectory) {
		this.downloadDirectory = downloadDirectory;
	}

	public Boolean getDetermineSubDirectory() {
		return determineSubDirectory;
	}

	public void setDetermineSubDirectory(Boolean determineSubDirectory) {
		this.determineSubDirectory = determineSubDirectory;
	}

	public Boolean getExtractRars() {
		return extractRars;
	}

	public void setExtractRars(Boolean extractRars) {
		this.extractRars = extractRars;
	}

	public String getSystemCommand() {
		return systemCommand;
	}

	public void setSystemCommand(String systemCommand) {
		this.systemCommand = systemCommand;
	}

	public Integer getSyncInterval() {
		return syncInterval;
	}

	public void setSyncInterval(Integer syncInterval) {
		this.syncInterval = syncInterval;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Integer getUploadLimit() {
		return uploadLimit;
	}

	public void setUploadLimit(Integer uploadLimit) {
		this.uploadLimit = uploadLimit;
	}

	public Integer getDeleteInterval() {
		return deleteInterval;
	}

	public void setDeleteInterval(Integer deleteInterval) {
		this.deleteInterval = deleteInterval;
	}

	public String getNotifyEmail() {
		return notifyEmail;
	}

	public void setNotifyEmail(String notifyEmail) {
		this.notifyEmail = notifyEmail;
	}

	public String getDetailsUrlValueFromRegex() {
		return detailsUrlValueFromRegex;
	}

	public void setDetailsUrlValueFromRegex(String detailsUrlValueFromRegex) {
		this.detailsUrlValueFromRegex = detailsUrlValueFromRegex;
	}

	public String getDetailsUrlFormat() {
		return detailsUrlFormat;
	}

	public void setDetailsUrlFormat(String detailsUrlFormat) {
		this.detailsUrlFormat = detailsUrlFormat;
	}

	public Boolean getSkipDuplicates() {
		return skipDuplicates;
	}

	public void setSkipDuplicates(Boolean skipDuplicates) {
		this.skipDuplicates = skipDuplicates;
	}

	public Boolean getSkipPropersRepacksReals() {
		return skipPropersRepacksReals;
	}

	public void setSkipPropersRepacksReals(Boolean skipPropersRepacksReals) {
		this.skipPropersRepacksReals = skipPropersRepacksReals;
	}

	public Boolean getPropersRepacksReplaceFile() {
		return propersRepacksReplaceFile;
	}

	public void setPropersRepacksReplaceFile(Boolean propersRepacksReplaceFile) {
		this.propersRepacksReplaceFile = propersRepacksReplaceFile;
	}

	public Boolean getRemoveTorrentOnComplete() {
		return removeTorrentOnComplete;
	}

	public void setRemoveTorrentOnComplete(Boolean removeTorrentOnComplete) {
		this.removeTorrentOnComplete = removeTorrentOnComplete;
	}

	public Boolean getRemoveTorrentDataOnComplete() {
		return removeTorrentDataOnComplete;
	}

	public void setRemoveTorrentDataOnComplete(Boolean removeTorrentDataOnComplete) {
		this.removeTorrentDataOnComplete = removeTorrentDataOnComplete;
	}

	public Boolean getFilterEnabled() {
		return filterEnabled;
	}

	public void setFilterEnabled(Boolean filterEnabled) {
		this.filterEnabled = filterEnabled;
	}

	public Boolean getRemoveAddFilterOnMatch() {
		return removeAddFilterOnMatch;
	}

	public void setRemoveAddFilterOnMatch(Boolean removeAddFilterOnMatch) {
		this.removeAddFilterOnMatch = removeAddFilterOnMatch;
	}

	public FilterAction getFilterAction() {
		return filterAction;
	}

	public void setFilterAction(FilterAction filterAction) {
		this.filterAction = filterAction;
	}

	public FilterAction getFilterPrecedence() {
		return filterPrecedence;
	}

	public void setFilterPrecedence(FilterAction filterPrecedence) {
		this.filterPrecedence = filterPrecedence;
	}

	public Set<FilterAttribute> getFilterAttributes() {
		return filterAttributes;
	}

	public void setFilterAttributes(Set<FilterAttribute> filterAttributes) {
		this.filterAttributes = filterAttributes;
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

	@Override
	public String toString() {
		return "FeedProvider [id=" + id + ", version=" + version + ", dateCreated=" + dateCreated
				+ ", lastProcessed=" + lastProcessed + ", name=" + name + ", feed=" + feed + ", active="
				+ active + ", downloadDirectory=" + downloadDirectory + ", determineSubDirectory="
				+ determineSubDirectory + ", extractRars=" + extractRars + ", systemCommand=" + systemCommand
				+ ", syncInterval=" + syncInterval + ", action=" + action + ", uploadLimit=" + uploadLimit
				+ ", deleteInterval=" + deleteInterval + ", notifyEmail=" + notifyEmail + ", detailsUrlValueFromRegex="
				+ detailsUrlValueFromRegex + ", detailsUrlFormat=" + detailsUrlFormat + ", skipDuplicates="
				+ skipDuplicates + ", skipPropersRepacksReals=" + skipPropersRepacksReals + ", removeTorrentOnComplete="
				+ removeTorrentOnComplete + ", removeTorrentDataOnComplete=" + removeTorrentDataOnComplete
				+ ", filterEnabled=" + filterEnabled + ", removeAddFilterOnMatch=" + removeAddFilterOnMatch
				+ ", filterAction=" + filterAction + ", filterPrecedence=" + filterPrecedence + ", filterAttributes="
				+ filterAttributes + ", torrentStates=" + torrentStates + ", messages=" + messages + "]";
	}
	
}
