package swordfishsync.service.dto;

import java.util.Date;

import swordfishsync.domain.Message;
import swordfishsync.domain.Message.Category;
import swordfishsync.domain.Message.Type;

public class MessageDto {

	Long			id;
	Date			dateCreated;
	Date			dateUpdated;
	Type			type;
	Category		category;
	FeedProviderDto	feedProvider;
	TorrentDto		torrent;
	String			message;
	Boolean			reported;

	public static MessageDto convertToMessageDto(Message message) {
		MessageDto messageDto = new MessageDto();
		
		messageDto.setId(message.getId());
		messageDto.setDateCreated(message.getDateCreated());
		messageDto.setDateUpdated(message.getDateUpdated());
		messageDto.setType(message.getType());
		messageDto.setCategory(message.getCategory());
		if (message.getFeedProvider() != null) {
			messageDto.setFeedProvider(FeedProviderDto.convertToFeedProviderDto(message.getFeedProvider()));
		}
		if (message.getTorrent() != null) {
			messageDto.setTorrent(TorrentDto.convertToTorrentDto(message.getTorrent()));
		}
		messageDto.setMessage(message.getMessage());
		messageDto.setReported(message.getReported());
		
		return messageDto;
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

	public FeedProviderDto getFeedProvider() {
		return feedProvider;
	}

	public void setFeedProvider(FeedProviderDto feedProvider) {
		this.feedProvider = feedProvider;
	}

	public TorrentDto getTorrent() {
		return torrent;
	}

	public void setTorrent(TorrentDto torrent) {
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
		return "MessageDto [id=" + id + ", dateCreated=" + dateCreated + ", dateUpdated=" + dateUpdated + ", type="
				+ type + ", category=" + category + ", feedProvider=" + feedProvider + ", torrent=" + torrent
				+ ", message=" + message + ", reported=" + reported + "]";
	}

}
