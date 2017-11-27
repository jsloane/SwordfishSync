package swordfishsync.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import swordfishsync.domain.Feed;
import swordfishsync.domain.FeedProvider;

public interface FeedRepository extends JpaRepository<Feed, Long> {
	
	Feed findByUrl(String url);
	
}
