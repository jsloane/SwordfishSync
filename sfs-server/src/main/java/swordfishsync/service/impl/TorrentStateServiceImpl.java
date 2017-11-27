package swordfishsync.service.impl;

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

    	Page<TorrentDto> torrentDtoPage = torrentStates.map(new Converter<TorrentState, TorrentDto>() {
    	    @Override
    	    public TorrentDto convert(TorrentState torrentState) {
    	    	TorrentDetails torrentDetails = null;
    	    	try {
					torrentDetails = torrentClientService.getTorrentDetails(torrentState.getTorrent(), false);
				} catch (TorrentClientException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	        return TorrentDto.convertToTorrentDto(torrentState, torrentDetails);
    	    }
    	});
		
		return torrentDtoPage;
	}

}
