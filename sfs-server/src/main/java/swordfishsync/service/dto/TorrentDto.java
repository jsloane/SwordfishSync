package swordfishsync.service.dto;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Version;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import swordfishsync.domain.Feed;
import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.FilterAttribute;
import swordfishsync.domain.Message;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;
import swordfishsync.model.TorrentDetails;
import swordfishsync.util.FeedProviderUtils;

public class TorrentDto {

	// torrent state fields
	Long				id;
	TorrentState.Status	status;
	
	// torrent fields
	Long				torrentId;
	Date				torrentDateAdded;
	String				torrentUrl;
	String				torrentName;
	String				torrentDetailsUrl;
	Date				torrentDatePublished;
	Date				torrentDateCompleted;
	String				torrentHashString;
	Integer				torrentClientTorrentId;
	Boolean				torrentInCurrentFeed;
	Boolean				torrentAddedToTorrentClient;
	
	// torrent feed provider fields
	String				detailsUrl;
	Long				feedProviderId;
	String				feedProviderName;
	
	// torrent details from client
	Date				clientActivityDate;
	Double				clientPercentDone;
	
	public static TorrentDto convertToTorrentDto(TorrentState torrentState) {
		TorrentDto torrentDto = new TorrentDto();
		
		torrentDto.setId(torrentState.getId());
		torrentDto.setStatus(torrentState.getStatus());

		torrentDto.setTorrentId(torrentState.getTorrent().getId());
		torrentDto.setTorrentUrl(torrentState.getTorrent().getUrl());
		torrentDto.setTorrentDateAdded(torrentState.getTorrent().getDateAdded());
		torrentDto.setTorrentUrl(torrentState.getTorrent().getUrl());
		torrentDto.setTorrentName(torrentState.getTorrent().getName());
		torrentDto.setTorrentDetailsUrl(torrentState.getTorrent().getDetailsUrl());
		torrentDto.setTorrentDatePublished(torrentState.getTorrent().getDatePublished());
		torrentDto.setTorrentDateCompleted(torrentState.getTorrent().getDateCompleted());
		torrentDto.setTorrentHashString(torrentState.getTorrent().getHashString());
		torrentDto.setTorrentClientTorrentId(torrentState.getTorrent().getClientTorrentId());
		torrentDto.setTorrentInCurrentFeed(torrentState.getTorrent().getInCurrentFeed());
		torrentDto.setTorrentAddedToTorrentClient(torrentState.getTorrent().getAddedToTorrentClient());

		torrentDto.setFeedProviderId(torrentState.getFeedProvider().getId());
		torrentDto.setFeedProviderName(torrentState.getFeedProvider().getName());
		
		torrentDto.setDetailsUrl(FeedProviderUtils.getTorrentDetailsUrl(torrentState));
		
		return torrentDto;
	}

	public static TorrentDto convertToTorrentDto(TorrentState torrentState, TorrentDetails torrentDetails) {
		TorrentDto torrentDto = convertToTorrentDto(torrentState);
		
		torrentDto.setClientActivityDate(torrentDetails.getActivityDate());
		torrentDto.setClientPercentDone(torrentDetails.getPercentDone());
		
		return torrentDto;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TorrentState.Status getStatus() {
		return status;
	}

	public void setStatus(TorrentState.Status status) {
		this.status = status;
	}

	public Long getTorrentId() {
		return torrentId;
	}

	public void setTorrentId(Long torrentId) {
		this.torrentId = torrentId;
	}

	public Date getTorrentDateAdded() {
		return torrentDateAdded;
	}

	public void setTorrentDateAdded(Date torrentDateAdded) {
		this.torrentDateAdded = torrentDateAdded;
	}

	public String getTorrentUrl() {
		return torrentUrl;
	}

	public void setTorrentUrl(String torrentUrl) {
		this.torrentUrl = torrentUrl;
	}

	public String getTorrentName() {
		return torrentName;
	}

	public void setTorrentName(String torrentName) {
		this.torrentName = torrentName;
	}

	public String getTorrentDetailsUrl() {
		return torrentDetailsUrl;
	}

	public void setTorrentDetailsUrl(String torrentDetailsUrl) {
		this.torrentDetailsUrl = torrentDetailsUrl;
	}

	public Date getTorrentDatePublished() {
		return torrentDatePublished;
	}

	public void setTorrentDatePublished(Date torrentDatePublished) {
		this.torrentDatePublished = torrentDatePublished;
	}

	public Date getTorrentDateCompleted() {
		return torrentDateCompleted;
	}

	public void setTorrentDateCompleted(Date torrentDateCompleted) {
		this.torrentDateCompleted = torrentDateCompleted;
	}

	public String getTorrentHashString() {
		return torrentHashString;
	}

	public void setTorrentHashString(String torrentHashString) {
		this.torrentHashString = torrentHashString;
	}

	public Integer getTorrentClientTorrentId() {
		return torrentClientTorrentId;
	}

	public void setTorrentClientTorrentId(Integer torrentClientTorrentId) {
		this.torrentClientTorrentId = torrentClientTorrentId;
	}

	public Boolean getTorrentInCurrentFeed() {
		return torrentInCurrentFeed;
	}

	public void setTorrentInCurrentFeed(Boolean torrentInCurrentFeed) {
		this.torrentInCurrentFeed = torrentInCurrentFeed;
	}

	public Boolean getTorrentAddedToTorrentClient() {
		return torrentAddedToTorrentClient;
	}

	public void setTorrentAddedToTorrentClient(Boolean torrentAddedToTorrentClient) {
		this.torrentAddedToTorrentClient = torrentAddedToTorrentClient;
	}

	public String getDetailsUrl() {
		return detailsUrl;
	}

	public void setDetailsUrl(String detailsUrl) {
		this.detailsUrl = detailsUrl;
	}

	public Long getFeedProviderId() {
		return feedProviderId;
	}

	public void setFeedProviderId(Long feedProviderId) {
		this.feedProviderId = feedProviderId;
	}

	public String getFeedProviderName() {
		return feedProviderName;
	}

	public void setFeedProviderName(String feedProviderName) {
		this.feedProviderName = feedProviderName;
	}

	public Date getClientActivityDate() {
		return clientActivityDate;
	}

	public void setClientActivityDate(Date clientActivityDate) {
		this.clientActivityDate = clientActivityDate;
	}

	public Double getClientPercentDone() {
		return clientPercentDone;
	}

	public void setClientPercentDone(Double clientPercentDone) {
		this.clientPercentDone = clientPercentDone;
	}

	@Override
	public String toString() {
		return "TorrentDto [id=" + id + ", status=" + status + ", torrentId=" + torrentId + ", torrentDateAdded="
				+ torrentDateAdded + ", torrentUrl=" + torrentUrl + ", torrentName=" + torrentName
				+ ", torrentDetailsUrl=" + torrentDetailsUrl + ", torrentDatePublished=" + torrentDatePublished
				+ ", torrentDateCompleted=" + torrentDateCompleted + ", torrentHashString=" + torrentHashString
				+ ", torrentClientTorrentId=" + torrentClientTorrentId + ", torrentInCurrentFeed="
				+ torrentInCurrentFeed + ", torrentAddedToTorrentClient=" + torrentAddedToTorrentClient
				+ ", detailsUrl=" + detailsUrl + ", feedProviderId=" + feedProviderId + ", feedProviderName="
				+ feedProviderName + ", clientActivityDate=" + clientActivityDate + ", clientPercentDone="
				+ clientPercentDone + "]";
	}

}
