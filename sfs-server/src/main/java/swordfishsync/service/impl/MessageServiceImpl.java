package swordfishsync.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import swordfishsync.domain.Feed;
import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Message;
import swordfishsync.domain.Message.Category;
import swordfishsync.domain.Message.Type;
import swordfishsync.domain.Torrent;
import swordfishsync.exceptions.ApplicationException;
import swordfishsync.repository.MessageRepository;
import swordfishsync.service.MessageService;
import swordfishsync.service.NotificationService;
import swordfishsync.service.dto.MessageDto;

@Transactional
@Service("messageService")
public class MessageServiceImpl implements MessageService {
	
    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

	@Resource
	NotificationService notificationService;
	
	@Resource
	MessageRepository messageRepository;
	
	@Override
	public Message logMessage(Boolean report, Type type, Category category, FeedProvider feedProvider, Torrent torrent, String messageString, Throwable throwable) {
		Date currentDate = new Date();
		
		Message message = null;
		
		if (feedProvider != null && torrent != null) {
			message = messageRepository.findByFeedProviderAndTorrentAndTypeAndCategory(feedProvider, torrent, type, category);
		} else if (feedProvider != null) {
			message = messageRepository.findByFeedProviderAndTypeAndCategoryAndTorrentIsNull(feedProvider, type, category);
		} else {
			message = messageRepository.findByTypeAndCategoryAndFeedProviderIsNullAndTorrentIsNull(type, category);
		}
		
		if (message == null) {
			message = new Message();
			message.setFeedProvider(feedProvider);
			message.setTorrent(torrent);
			message.setCategory(category);
			message.setType(type);
			message.setDateCreated(currentDate);
			message.setReported(false);
		}
		
		message.setDateUpdated(currentDate);
		message.setMessage(StringUtils.abbreviate(messageString, 1024));
		
		message = messageRepository.save(message);
		
		if (!message.getReported() && report) {
			try {
				notificationService.sendMessageReport(message, throwable);
				message.setReported(true);
				message = messageRepository.save(message);
			} catch (ApplicationException e) {
				log.error("Error sending message report email", e);
			}
		}
		
		return message;
	}

	@Override
	public List<MessageDto> getMessages() {
		List<MessageDto> messageDtos = new ArrayList<MessageDto>();
		
		List<Message> messages = messageRepository.findAll();
		
		for (Message message : messages) {
			messageDtos.add(MessageDto.convertToMessageDto(message));
		}
		
		return messageDtos;
	}

	@Override
	public void deleteMessage(Long id) {
		messageRepository.deleteById(id);
	}

}
