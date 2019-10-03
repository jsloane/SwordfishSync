package swordfishsync.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import swordfishsync.domain.TorrentState;
import swordfishsync.exceptions.TorrentClientException;
import swordfishsync.model.TorrentDetails;
import swordfishsync.service.TorrentClientService;
import swordfishsync.service.TorrentStateService;
import swordfishsync.service.dto.TorrentDto;

@RestController
@RequestMapping("/api")
public class TorrentController {

    private final Logger log = LoggerFactory.getLogger(TorrentController.class);

	@Resource
	TorrentStateService torrentStateService;
	
	@Resource
	TorrentClientService torrentClientService;

    @RequestMapping(value = "/torrents/torrentStatesByStatus", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Page<TorrentDto>> getTorrentsByStatuses(@RequestParam List<String> statuses, Pageable pageable) {
    	List<TorrentState.Status> torrentStateStatuses = new ArrayList<TorrentState.Status>();
    	for (String status : statuses) {
    		switch (status) {
				case "downloading":
					torrentStateStatuses.add(TorrentState.Status.IN_PROGRESS);
					break;
				case "notified":
					torrentStateStatuses.add(TorrentState.Status.NOTIFIED_NOT_ADDED);
					break;
				case "completed":
					torrentStateStatuses.add(TorrentState.Status.COMPLETED);
					torrentStateStatuses.add(TorrentState.Status.NOTIFY_COMPLETED);
					break;
    			default:
    				break;
    		}
    	}

    	Page<TorrentDto> page = torrentStateService.getTorrentStatesByStatuses(torrentStateStatuses, pageable);
        return new ResponseEntity<Page<TorrentDto>>(page, HttpStatus.OK);
    }

    @RequestMapping(value = "/torrents/clientTorrents", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<TorrentDetails>> getClientTorrents() {
    	List<TorrentDetails> torrents = null;
		try {
			torrents = torrentClientService.getAllTorrents();
		} catch (TorrentClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
        	return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
    	
        return new ResponseEntity<List<TorrentDetails>>(torrents, HttpStatus.OK);
    }

    @RequestMapping(value = "/torrents/purgeInProgress", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purgeTorrents() {
    	torrentStateService.purgeTorrentStates(Arrays.asList(TorrentState.Status.IN_PROGRESS));
    }
    
}
