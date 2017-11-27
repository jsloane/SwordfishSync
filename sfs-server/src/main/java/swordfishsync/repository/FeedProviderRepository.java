package swordfishsync.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import swordfishsync.domain.Feed;
import swordfishsync.domain.FeedProvider;

public interface FeedProviderRepository extends JpaRepository<FeedProvider, Long> {

	List<FeedProvider> findAllByActive(boolean active);

	List<FeedProvider> findAllByFeed(Feed feed);
	
}
