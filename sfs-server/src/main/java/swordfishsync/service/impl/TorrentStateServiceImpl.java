package swordfishsync.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import swordfishsync.domain.TorrentState;
import swordfishsync.domain.TorrentState.Status;
import swordfishsync.exceptions.TorrentClientException;
import swordfishsync.model.TorrentDetails;
import swordfishsync.repository.TorrentStateRepository;
import swordfishsync.service.FeedProviderService;
import swordfishsync.service.TorrentClientService;
import swordfishsync.service.TorrentStateService;
import swordfishsync.service.dto.FeedProviderDto;
import swordfishsync.service.dto.TorrentDto;

@Transactional
@Service("torrentStateService")
public class TorrentStateServiceImpl implements TorrentStateService {

    private final Logger log = LoggerFactory.getLogger(TorrentStateService.class);
    
	@Resource
	TorrentStateRepository torrentStateRepository;
	
	@Resource
	TorrentClientService torrentClientService;
	
	@Override
	public Page<TorrentDto> getTorrentStatesByStatuses(List<Status> statuses, Pageable pageable) {
		Page<TorrentState> torrentStates = torrentStateRepository.findAllByStatusIn(statuses, pageable);

    	/*Page<TorrentDto> torrentDtoPage = torrentStates.map(new Converter<TorrentState, TorrentDto>() {
    	    @Override
    	    public TorrentDto convert(TorrentState torrentState) {
    	    	TorrentDetails torrentDetails = null;
    	    	try {
					torrentDetails = torrentClientService.getTorrentDetails(torrentState.getTorrent(), false);
				} catch (TorrentClientException e) {
					log.error("Error loading torrent details for torrent [" + torrentState.getTorrent().getName() + "]", e);
				}
    	        return TorrentDto.convertToTorrentDto(torrentState, torrentDetails);
    	    }
    	});*/
    	Page<TorrentDto> torrentDtoPage = torrentStates.map(torrentState -> {
	    	TorrentDetails torrentDetails = null;
	    	try {
				torrentDetails = torrentClientService.getTorrentDetails(torrentState.getTorrent(), false);
			} catch (TorrentClientException e) {
				log.error("Error loading torrent details for torrent [" + torrentState.getTorrent().getName() + "]", e);
			}
	        return TorrentDto.convertToTorrentDto(torrentState, torrentDetails);
    	});

		return torrentDtoPage;
	}

	@Override
	public List<String> purgeTorrentStates(List<Status> statuses) {
    	List<String> messages = new ArrayList<String>();
    	
    	List<TorrentState> torrentStates = torrentStateRepository.findAllByStatusIn(statuses);
    	
    	for (TorrentState torrentState : torrentStates) {
    		try {
    			TorrentDetails torrentDetails = torrentClientService.getTorrentDetails(torrentState.getTorrent(), false);
    			if (torrentDetails == null || TorrentDetails.Status.UNKNOWN.equals(torrentDetails.getStatus())) {
    				torrentState.setStatus(TorrentState.Status.SKIPPED);
    				torrentStateRepository.save(torrentState);
    		    	messages.add("Purged torrent [" + torrentState.getTorrent().getName() + "]");
    			}
			} catch (TorrentClientException e) {
		    	messages.add("Error retrieving torrent details for torrent [" + torrentState.getTorrent().getName() + "]. Error: " + e.toString());
				log.error("Error retrieving torrent details for torrent [" + torrentState.getTorrent().getName() + "]", e);
			}
    	}

    	return messages;
	}

}
