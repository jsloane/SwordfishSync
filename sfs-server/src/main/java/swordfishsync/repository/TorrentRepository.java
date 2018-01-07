package swordfishsync.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import swordfishsync.domain.Feed;
import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState.Status;

public interface TorrentRepository extends JpaRepository<Torrent, Long> {

	
	@Modifying
	@Query("update Torrent t set t.inCurrentFeed = ?1 where t.inCurrentFeed = ?2 and t.feed = ?3")
	int setInCurrentFeedByInCurrentFeedAndFeed(boolean inCurrentFeed, boolean whereInCurrentFeed, Feed feed); // TODO use @param names

	Torrent findByFeedAndUrl(Feed feed, String url);
/*
	List<Torrent> findAllByTorrentStatesFeedProviderAndTorrentStatesStatusIn(FeedProvider feedProvider, List<Status> statuses);

	List<Torrent> findAllByTorrentStatesFeedProviderAndTorrentStatesStatusNotIn(FeedProvider feedProvider, List<Status> statuses);

	List<Torrent> findAllByTorrentStatesFeedProviderAndTorrentStatesStatusInAndDateAddedBefore(FeedProvider feedProvider, List<Status> statuses, Date dateAddedBefore);

	List<Torrent> findAllByTorrentStatesFeedProviderAndTorrentStatesStatusNotInAndDateAddedBefore(FeedProvider feedProvider, List<Status> statuses, Date dateAddedBefore);
	*/
	//boolean existsBySubjectOfferingDescriptor(String subjectOfferingDescriptor);
	//void deleteByTorrentStatesIsNull()
	//boolean existsByTorrentStatesTorrent(Torrent torrent);

	@Modifying
	@Query("delete from Torrent t where t not in (select ts.torrent from TorrentState ts)")
	void deleteAllWithEmptyTorrentStates();
	
	/*void deleteByRoleId(long roleId);

	@Modifying
	@Query("delete from User u where user.role.id = ?1")
	void deleteInBulkByRoleId(long roleId);*/
	
}
