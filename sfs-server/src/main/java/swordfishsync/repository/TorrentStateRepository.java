package swordfishsync.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;
import swordfishsync.domain.TorrentState.Status;

public interface TorrentStateRepository extends JpaRepository<TorrentState, Long> {

	TorrentState findByIdAndFeedProviderId(Long id, Long feedProviderId);

	TorrentState findByFeedProviderAndTorrent(FeedProvider feedProvider, Torrent torrent);

	TorrentState findByFeedProviderIdAndTorrentId(Long feedProviderId, Long torrentId);

	TorrentState findByFeedProviderIdAndTorrentUrl(Long id, String torrentUrl);
	
	void deleteByFeedProviderAndStatusInAndTorrentDateAddedBeforeAndTorrentInCurrentFeed(
			FeedProvider feedProvider, List<Status> statuses, Date dateAddedBefore, Boolean inCurrentFeed);

	List<TorrentState> findAllByFeedProviderAndStatusInAndTorrentDateAddedBefore(FeedProvider feedProvider, List<Status> statuses, Date dateAddedBefore);

	List<TorrentState> findAllByFeedProviderAndStatusNotInAndTorrentDateAddedBefore(FeedProvider feedProvider, List<Status> statuses, Date dateAddedBefore);


	List<TorrentState> findAllByFeedProviderAndStatusIn(FeedProvider feedProvider, List<Status> statuses);

	List<TorrentState> findAllByFeedProviderAndStatusNotIn(FeedProvider feedProvider, List<Status> statuses);

	Page<TorrentState> findAllByFeedProviderId(Long id, Pageable pageable);
	
	Page<TorrentState> findAllByStatusIn(List<Status> statuses, Pageable pageable);
	
	
	//@Modifying
	//@Query("delete from TorrentState t where t.id = ?1")
	//void deleteInBulkByFeedProviderAndTorrentDateAddedBeforeAndStatusNotIn(long roleId);
	
}
