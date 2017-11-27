package swordfishsync.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import swordfishsync.domain.Feed;
import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.FilterAttribute;

public interface FilterAttributeRepository extends JpaRepository<FilterAttribute, Long> {
	
	List<FilterAttribute> findAllByFeedProviderId(Long feedProviderId);

	void deleteByFeedProviderId(Long feedProviderId);
	
	@Modifying
	@Query("delete from FilterAttribute f where f.feedProvider.id = ?1")
	void deleteInBulkByFeedProviderId(Long feedProviderId);
	
}
