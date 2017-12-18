package swordfishsync.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import swordfishsync.controllers.TorrentController;
import swordfishsync.domain.Feed;
import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.FilterAttribute;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;
import swordfishsync.exceptions.NotFoundException;
import swordfishsync.exceptions.TorrentClientException;
import swordfishsync.repository.FeedProviderRepository;
import swordfishsync.repository.FeedRepository;
import swordfishsync.repository.FilterAttributeRepository;
import swordfishsync.repository.TorrentRepository;
import swordfishsync.repository.TorrentStateRepository;
import swordfishsync.service.FeedProviderService;
import swordfishsync.service.TorrentClientService;
import swordfishsync.service.dto.FeedProviderDto;
import swordfishsync.service.dto.FilterAttributeDto;
import swordfishsync.service.dto.TorrentDto;

@Transactional
@Service("feedProviderService")
public class FeedProviderServiceImpl implements FeedProviderService {

    private final Logger log = LoggerFactory.getLogger(FeedProviderService.class);
    
	@Resource
	FeedProviderRepository feedProviderRepository;
	
	@Resource
	FeedRepository feedRepository;
	
	@Resource
	TorrentStateRepository torrentStateRepository;

	@Resource
	TorrentRepository torrentRepository;
	
	@Resource
	FilterAttributeRepository filterAttributeRepository;
	
	@Resource
	TorrentClientService torrentClientService;

	@Override
	public Page<FeedProviderDto> findAllFeedProviders(Pageable pageable) {
    	Page<FeedProvider> feedProviderPage = feedProviderRepository.findAll(pageable);

    	Page<FeedProviderDto> dtoPage = feedProviderPage.map(new Converter<FeedProvider, FeedProviderDto>() {
    	    @Override
    	    public FeedProviderDto convert(FeedProvider entity) {
    	        return FeedProviderDto.convertToFeedProviderDto(entity);
    	    }
    	});
    	
		return dtoPage;
	}
	
	@Override
	public FeedProviderDto createFeedProvider(FeedProviderDto feedProviderDto) {
		
		FeedProvider feedProvider = new FeedProvider();
		
		Feed feed = feedRepository.findByUrl(feedProviderDto.getFeedUrl());
		
		if (feed == null) {
			feed = new Feed();
			feed.setUrl(feedProviderDto.getFeedUrl());
			feed = feedRepository.save(feed);
		}
		
		feedProvider.setFeed(feed);
		
		setFeedProviderFields(feedProvider, feedProviderDto);
		
		feedProvider = feedProviderRepository.save(feedProvider);

		return FeedProviderDto.convertToFeedProviderDto(feedProvider);
		
	}

	@Override
	public FeedProviderDto getFeedProvider(Long id) {
		FeedProvider feedProvider = feedProviderRepository.findOne(id);
		
		if (feedProvider == null) {
			throw new NotFoundException(String.format("FeedProvider with id [%d] not found", id));
		}

		return FeedProviderDto.convertToFeedProviderDto(feedProvider);
	}

	@Override
	public FeedProviderDto updateFeedProvider(Long id, FeedProviderDto feedProviderDto) {
		FeedProvider feedProvider = feedProviderRepository.findOne(id);
		
		if (feedProvider == null) {
			throw new NotFoundException(String.format("FeedProvider with id [%d] not found", id));
		}
		
		if (!feedProvider.getFeed().getUrl().equals(feedProviderDto.getFeedUrl())) {
			Feed oldFeed = feedProvider.getFeed();

			
			// create new feed
			Feed feed = new Feed();
			feed.setUrl(feedProviderDto.getFeedUrl());
			feed = feedRepository.save(feed);
			feedProvider.setFeed(feed);

			
			// TODO delete old torrent status
			// TODO find if feed has any other feed providers, and remove the feed if not
			// TODO see how grails did it
		}
		
		setFeedProviderFields(feedProvider, feedProviderDto);

		feedProvider = feedProviderRepository.save(feedProvider);
		
		return FeedProviderDto.convertToFeedProviderDto(feedProvider);
	}

	private void setFeedProviderFields(FeedProvider feedProvider, FeedProviderDto feedProviderDto) {
		feedProvider.setName(feedProviderDto.getName());
		feedProvider.setActive(feedProviderDto.getActive());
		feedProvider.setDownloadDirectory(feedProviderDto.getDownloadDirectory());
		feedProvider.setDetermineSubDirectory(feedProviderDto.getDetermineSubDirectory());
		feedProvider.setExtractRars(feedProviderDto.getExtractRars());
		feedProvider.setSystemCommand(feedProviderDto.getSystemCommand());
		feedProvider.setSyncInterval(feedProviderDto.getSyncInterval());
		feedProvider.setAction(feedProviderDto.getAction());
		feedProvider.setUploadLimit(feedProviderDto.getUploadLimit());
		feedProvider.setDeleteInterval(feedProviderDto.getDeleteInterval());
		feedProvider.setNotifyEmail(feedProviderDto.getNotifyEmail());
		feedProvider.setDetailsUrlValueFromRegex(feedProviderDto.getDetailsUrlValueFromRegex());
		feedProvider.setDetailsUrlFormat(feedProviderDto.getDetailsUrlFormat());
		feedProvider.setSkipDuplicates(feedProviderDto.getSkipDuplicates());
		feedProvider.setSkipPropersRepacksReals(feedProviderDto.getSkipPropersRepacksReals());
		feedProvider.setRemoveTorrentOnComplete(feedProviderDto.getRemoveTorrentOnComplete());
		feedProvider.setRemoveTorrentDataOnComplete(feedProviderDto.getRemoveTorrentDataOnComplete());
		feedProvider.setFilterEnabled(feedProviderDto.getFilterEnabled());
		feedProvider.setRemoveAddFilterOnMatch(feedProviderDto.getRemoveAddFilterOnMatch());
		feedProvider.setFilterAction(feedProviderDto.getFilterAction());
		feedProvider.setFilterPrecedence(feedProviderDto.getFilterPrecedence());
	}

	@Override
	public void deleteFeedProvider(Long id) {
		feedProviderRepository.delete(id);
		// TODO delete old torrent status? might already be done as part of sync service?
		// TODO find if feed has any other feed providers, and remove the feed if not. might already be done as part of sync service?
		// TODO see how grails did it
	}

	@Override
	public Page<TorrentDto> findAllFeedProviderTorrents(Long id, Pageable pageable) {
    	Page<TorrentState> torrentStatePage = torrentStateRepository.findAllByFeedProviderId(id, pageable);

    	Page<TorrentDto> torrentDtoPage = torrentStatePage.map(new Converter<TorrentState, TorrentDto>() {
    	    @Override
    	    public TorrentDto convert(TorrentState entity) {
    	        return TorrentDto.convertToTorrentDto(entity);
    	    }
    	});
    	
		return torrentDtoPage;
	}

	@Override
	public List<FilterAttributeDto> replaceFeedProviderFilterAttributes(Long feedProviderId, List<FilterAttributeDto> filterAttributeDtos) {
		FeedProvider feedProvider = feedProviderRepository.findOne(feedProviderId);
		
		filterAttributeRepository.deleteInBulkByFeedProviderId(feedProviderId);
		
		List<FilterAttribute> filterAttributes = new ArrayList<FilterAttribute>();
		for (FilterAttributeDto filterAttributeDto : filterAttributeDtos) {
			FilterAttribute filterAttribute = new FilterAttribute();
			filterAttribute.setFilterRegex(filterAttributeDto.getFilterRegex());
			filterAttribute.setFilterType(filterAttributeDto.getFilterType());
			filterAttribute.setFeedProvider(feedProvider);
			filterAttributes.add(filterAttribute);
		}
		
		List<FilterAttribute> savedFilterAttributes = filterAttributeRepository.save(filterAttributes);

		List<FilterAttributeDto> savedFilterAttributeDtos = new ArrayList<FilterAttributeDto>();
		for (FilterAttribute savedFilterAttribute : savedFilterAttributes) {
			savedFilterAttributeDtos.add(FilterAttributeDto.convertToFilterAttributeDto(savedFilterAttribute));
		}
		
		return savedFilterAttributeDtos;
	}

	@Override
	public List<FilterAttributeDto> getFeedProviderFilterAttributes(Long id) {
		List<FilterAttribute> filterAttributes = filterAttributeRepository.findAllByFeedProviderId(id);

		List<FilterAttributeDto> filterAttributeDtos = new ArrayList<FilterAttributeDto>();
		for (FilterAttribute filterAttribute : filterAttributes) {
			filterAttributeDtos.add(FilterAttributeDto.convertToFilterAttributeDto(filterAttribute));
		}
		
		return filterAttributeDtos;
	}

	@Override
	public boolean downloadTorrent(Long id, Long torrentStateId) throws TorrentClientException {
		boolean downloading = false;

		TorrentState torrentState = torrentStateRepository.findByIdAndFeedProviderId(torrentStateId, id);
		if (torrentState != null && !torrentState.getStatus().equals(TorrentState.Status.IN_PROGRESS)
				&& !torrentState.getStatus().equals(TorrentState.Status.COMPLETED)
				 && !torrentState.getStatus().equals(TorrentState.Status.NOTIFY_COMPLETED)) {
				downloading = torrentClientService.addTorrent(torrentState);
		}
		
		return downloading;
	}

	@Override
	public List<TorrentDto> addTorrent(Long id, List<String> torrentUrls) throws TorrentClientException {
		List<TorrentDto> addedTorrents = new ArrayList<TorrentDto>();
		
		for (String torrentUrl : torrentUrls) {
			if (StringUtils.isNotBlank(torrentUrl)) {
				TorrentState newTorrentState = torrentStateRepository.findByFeedProviderIdAndTorrentUrl(id, torrentUrl);
				
				if (newTorrentState == null) {
					FeedProvider feedProvider = feedProviderRepository.findOne(id);
		
					Torrent newTorrent = new Torrent();
					newTorrent.setFeed(feedProvider.getFeed());
					newTorrent.setInCurrentFeed(false);
					newTorrent.setAddedToTorrentClient(false);
					newTorrent.setUrl(torrentUrl);
					newTorrent.setDateAdded(new Date());
					newTorrent = torrentRepository.saveAndFlush(newTorrent);
					
					newTorrentState = new TorrentState();
					newTorrentState.setTorrent(newTorrent);
					newTorrentState.setFeedProvider(feedProvider);
					newTorrentState.setStatus(TorrentState.Status.NOT_ADDED);
					newTorrentState = torrentStateRepository.saveAndFlush(newTorrentState);
				}
				
				if (!newTorrentState.getTorrent().getAddedToTorrentClient()) {
					torrentClientService.addTorrent(newTorrentState);
					addedTorrents.add(TorrentDto.convertToTorrentDto(newTorrentState));
				}
			}
		}
		
		return addedTorrents;
	}

}
