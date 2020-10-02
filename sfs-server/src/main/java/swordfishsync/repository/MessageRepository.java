package swordfishsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import swordfishsync.domain.Feed;
import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Message;
import swordfishsync.domain.Message.Category;
import swordfishsync.domain.Message.Type;
import swordfishsync.domain.Torrent;

public interface MessageRepository extends JpaRepository<Message, Long> {

	Message findByFeedProviderAndTorrentAndTypeAndCategory(FeedProvider feedProvider, Torrent torrent, Type type, Category category);

	Message findByFeedProviderAndTypeAndCategoryAndTorrentIsNull(FeedProvider feedProvider, Type type, Category category);

	Message findByTypeAndCategoryAndFeedProviderIsNullAndTorrentIsNull(Type type, Category category);

}
