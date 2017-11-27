package swordfishsync.service.dto;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import swordfishsync.domain.Feed;
import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.FilterAttribute;
import swordfishsync.domain.Message;
import swordfishsync.domain.TorrentState;
import swordfishsync.domain.FeedProvider.Action;
import swordfishsync.domain.FeedProvider.FilterAction;

public class FeedProviderDto {

	// feed provider fields
	Long id;
	Date dateCreated;
	Date lastUpdated;
	Date lastProcessed;
	String name;
	//Feed feed;
	Boolean active = false;
	String downloadDirectory;
	Boolean determineSubDirectory = false;
	Boolean extractRars = true;
	String systemCommand;
	Integer syncInterval = 0;
	Action action = FeedProvider.Action.SKIP;
	Integer uploadLimit = 0;
	Integer deleteInterval = 90;
	String notifyEmail;
	String detailsUrlValueFromRegex;
	String detailsUrlFormat;
	Boolean skipDuplicates = true;
	Boolean skipPropersRepacksReals = false;
	Boolean removeTorrentOnComplete = false;
	Boolean removeTorrentDataOnComplete = false;
	Boolean filterEnabled = true;
	Boolean removeAddFilterOnMatch = false;
	FilterAction filterAction = FeedProvider.FilterAction.IGNORE;
	FilterAction filterPrecedence = FeedProvider.FilterAction.IGNORE;
	Set<FilterAttribute> filterAttributes = new TreeSet<FilterAttribute>();
	Set<Message> messages = new TreeSet<Message>();

	// feed fields
	String feedUrl;
	Boolean feedInitilised;
	Boolean feedInitialPopulate;
	Date feedLastFetched;
	Date feedLastPurged;
	Integer feedTtl;
	Boolean feedIsCurrent;
	
	public static FeedProviderDto convertToFeedProviderDto(final FeedProvider feedProvider) {
		FeedProviderDto feedProviderDto = new FeedProviderDto();

		feedProviderDto.setId(feedProvider.getId());

		feedProviderDto.setFeedUrl(feedProvider.getFeed().getUrl());
		feedProviderDto.setFeedInitilised(feedProvider.getFeed().getInitilised());
		feedProviderDto.setFeedInitialPopulate(feedProvider.getFeed().getInitialPopulate());
		feedProviderDto.setFeedLastFetched(feedProvider.getFeed().getLastFetched());
		feedProviderDto.setFeedTtl(feedProvider.getFeed().getTtl());
		feedProviderDto.setFeedIsCurrent(feedProvider.getFeed().getIsCurrent());
		feedProviderDto.setFeedLastPurged(feedProvider.getFeed().getLastPurged());
		
		feedProviderDto.setName(feedProvider.getName());
		feedProviderDto.setActive(feedProvider.getActive());
		feedProviderDto.setLastProcessed(feedProvider.getLastProcessed());
		feedProviderDto.setDownloadDirectory(feedProvider.getDownloadDirectory());
		feedProviderDto.setDetermineSubDirectory(feedProvider.getDetermineSubDirectory());
		feedProviderDto.setExtractRars(feedProvider.getExtractRars());
		feedProviderDto.setSystemCommand(feedProvider.getSystemCommand());
		feedProviderDto.setSyncInterval(feedProvider.getSyncInterval());
		feedProviderDto.setAction(feedProvider.getAction());
		feedProviderDto.setUploadLimit(feedProvider.getUploadLimit());
		feedProviderDto.setDeleteInterval(feedProvider.getDeleteInterval());
		feedProviderDto.setNotifyEmail(feedProvider.getNotifyEmail());
		feedProviderDto.setDetailsUrlValueFromRegex(feedProvider.getDetailsUrlValueFromRegex());
		feedProviderDto.setDetailsUrlFormat(feedProvider.getDetailsUrlFormat());
		feedProviderDto.setSkipDuplicates(feedProvider.getSkipDuplicates());
		feedProviderDto.setSkipPropersRepacksReals(feedProvider.getSkipPropersRepacksReals());
		feedProviderDto.setRemoveTorrentOnComplete(feedProvider.getRemoveTorrentOnComplete());
		feedProviderDto.setRemoveTorrentDataOnComplete(feedProvider.getRemoveTorrentDataOnComplete());
		feedProviderDto.setFilterEnabled(feedProvider.getFilterEnabled());
		feedProviderDto.setRemoveAddFilterOnMatch(feedProvider.getRemoveAddFilterOnMatch());
		feedProviderDto.setFilterAction(feedProvider.getFilterAction());
		feedProviderDto.setFilterPrecedence(feedProvider.getFilterPrecedence());

		return feedProviderDto;
	}

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

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
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

	public Set<Message> getMessages() {
		return messages;
	}

	public void setMessages(Set<Message> messages) {
		this.messages = messages;
	}

	public String getFeedUrl() {
		return feedUrl;
	}

	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
	}

	public Boolean getFeedInitilised() {
		return feedInitilised;
	}

	public void setFeedInitilised(Boolean feedInitilised) {
		this.feedInitilised = feedInitilised;
	}

	public Boolean getFeedInitialPopulate() {
		return feedInitialPopulate;
	}

	public void setFeedInitialPopulate(Boolean feedInitialPopulate) {
		this.feedInitialPopulate = feedInitialPopulate;
	}

	public Date getFeedLastFetched() {
		return feedLastFetched;
	}

	public void setFeedLastFetched(Date feedLastFetched) {
		this.feedLastFetched = feedLastFetched;
	}

	public Date getFeedLastPurged() {
		return feedLastPurged;
	}

	public void setFeedLastPurged(Date feedLastPurged) {
		this.feedLastPurged = feedLastPurged;
	}

	public Integer getFeedTtl() {
		return feedTtl;
	}

	public void setFeedTtl(Integer feedTtl) {
		this.feedTtl = feedTtl;
	}

	public Boolean getFeedIsCurrent() {
		return feedIsCurrent;
	}

	public void setFeedIsCurrent(Boolean feedIsCurrent) {
		this.feedIsCurrent = feedIsCurrent;
	}

	@Override
	public String toString() {
		return "FeedProviderDto [id=" + id + ", dateCreated=" + dateCreated + ", lastUpdated=" + lastUpdated
				+ ", lastProcessed=" + lastProcessed + ", name=" + name + ", active=" + active + ", downloadDirectory="
				+ downloadDirectory + ", determineSubDirectory=" + determineSubDirectory + ", extractRars="
				+ extractRars + ", systemCommand=" + systemCommand + ", syncInterval=" + syncInterval + ", action="
				+ action + ", uploadLimit=" + uploadLimit + ", deleteInterval=" + deleteInterval + ", notifyEmail="
				+ notifyEmail + ", detailsUrlValueFromRegex=" + detailsUrlValueFromRegex + ", detailsUrlFormat="
				+ detailsUrlFormat + ", skipDuplicates=" + skipDuplicates + ", skipPropersRepacksReals="
				+ skipPropersRepacksReals + ", removeTorrentOnComplete=" + removeTorrentOnComplete
				+ ", removeTorrentDataOnComplete=" + removeTorrentDataOnComplete + ", filterEnabled=" + filterEnabled
				+ ", removeAddFilterOnMatch=" + removeAddFilterOnMatch + ", filterAction=" + filterAction
				+ ", filterPrecedence=" + filterPrecedence + ", filterAttributes=" + filterAttributes + ", messages="
				+ messages + ", feedUrl=" + feedUrl + ", feedInitilised=" + feedInitilised + ", feedInitialPopulate="
				+ feedInitialPopulate + ", feedLastFetched=" + feedLastFetched + ", feedLastPurged=" + feedLastPurged
				+ ", feedTtl=" + feedTtl + ", feedIsCurrent=" + feedIsCurrent + "]";
	}
	
}
