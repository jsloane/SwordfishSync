package swordfishsync.service;

import java.util.List;

import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Message;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;
import swordfishsync.service.dto.MessageDto;

public interface MessageService {

	Message logMessage(Boolean report, Message.Type type, Message.Category category, FeedProvider feedProvider, Torrent torrent, String messageString);
	
	List<MessageDto> getMessages();
	
	void deleteMessage(Long id);

}
