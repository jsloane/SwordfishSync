package swordfishsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import swordfishsync.domain.Feed;

public interface FeedRepository extends JpaRepository<Feed, Long> {
	
	Feed findByUrl(String url);
	
}
