package swordfishsync.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import swordfishsync.exceptions.TorrentClientException;
import swordfishsync.service.dto.FeedProviderDto;
import swordfishsync.service.dto.FilterAttributeDto;
import swordfishsync.service.dto.TorrentDto;

public interface FeedProviderService {

	Page<FeedProviderDto> findAllFeedProviders(Pageable pageable);
	
	FeedProviderDto createFeedProvider(FeedProviderDto feedProviderDto);

	FeedProviderDto getFeedProvider(Long id);

	FeedProviderDto updateFeedProvider(Long id, FeedProviderDto feedProviderDto);

	void deleteFeedProvider(Long id);
	
	Page<TorrentDto> findAllFeedProviderTorrents(Long id, Pageable pageable);

	List<FilterAttributeDto> replaceFeedProviderFilterAttributes(Long feedProviderId, List<FilterAttributeDto> filterAttributeDtos);

	List<FilterAttributeDto> getFeedProviderFilterAttributes(Long id);
	
	boolean downloadTorrent(Long id, Long torrentStateId) throws TorrentClientException;

	List<TorrentDto> addTorrent(Long id, List<String> torrentUrls) throws TorrentClientException;

}
