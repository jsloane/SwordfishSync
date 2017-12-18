package swordfishsync.controllers;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import swordfishsync.domain.FeedProvider;
import swordfishsync.exceptions.TorrentClientException;
import swordfishsync.repository.FeedProviderRepository;
import swordfishsync.service.FeedProviderService;
import swordfishsync.service.TorrentClientService;
import swordfishsync.service.dto.FeedProviderDto;
import swordfishsync.service.dto.FilterAttributeDto;
import swordfishsync.service.dto.TorrentDto;

@RestController
@RequestMapping("/api")
public class FeedProviderController {

    private final Logger log = LoggerFactory.getLogger(FeedProviderController.class);
    
	@Resource
	FeedProviderService feedProviderService;
	
	@Resource
	TorrentClientService torrentClientService;

    @GetMapping("/feedProviders")
    @ResponseBody
    //@Timed //?
    public ResponseEntity<Page<FeedProviderDto>> getAllFeedProviders(Pageable pageable) {
    	Page<FeedProviderDto> page = feedProviderService.findAllFeedProviders(pageable);
        return new ResponseEntity<Page<FeedProviderDto>>(page, HttpStatus.OK);
    }

    @RequestMapping(value = "/feedProviders", method = RequestMethod.POST)
    @ResponseBody
    //@Timed //?
    public ResponseEntity<FeedProviderDto> createFeedProvider(@RequestBody FeedProviderDto feedProviderDto) {
        //final Page<FeedProviderDto> page = feedProviderService.getAllFeedProviders(pageable);
    	//Page<FeedProvider> page = feedProviderRepository.findAll(pageable);
        //HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
    	System.out.println("create feedProvider: " + feedProviderDto);
    	FeedProviderDto createdFeedProviderDto = feedProviderService.createFeedProvider(feedProviderDto);
    	
        return new ResponseEntity<FeedProviderDto>(createdFeedProviderDto, HttpStatus.CREATED);
    }

    @GetMapping("/feedProviders/{id}")
    @ResponseBody
    public ResponseEntity<FeedProviderDto> getFeedProvider(@PathVariable Long id) {
    	FeedProviderDto feedProviderDto = feedProviderService.getFeedProvider(id);
    	
        return new ResponseEntity<FeedProviderDto>(feedProviderDto, HttpStatus.OK);
    }

    @RequestMapping(value = "/feedProviders/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<FeedProviderDto> updateFeedProvider(@PathVariable Long id, @RequestBody FeedProviderDto feedProviderDto) {
    	FeedProviderDto updatedFeedProviderDto = feedProviderService.updateFeedProvider(id, feedProviderDto);
    	
        return new ResponseEntity<FeedProviderDto>(updatedFeedProviderDto, HttpStatus.OK);
    }

    @RequestMapping(value = "/feedProviders/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFeedProvider(@PathVariable Long id) {
    	feedProviderService.deleteFeedProvider(id);
    }

    @GetMapping("/feedProviders/{id}/torrents")
    @ResponseBody
    //@Timed //?
    public ResponseEntity<Page<TorrentDto>> getFeedProviderTorrents(@PathVariable Long id, Pageable pageable) {
    	Page<TorrentDto> page = feedProviderService.findAllFeedProviderTorrents(id, pageable);
        return new ResponseEntity<Page<TorrentDto>>(page, HttpStatus.OK);
    }

    @RequestMapping(value = "/feedProviders/{id}/torrents", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<List<TorrentDto>> addFeedProviderTorrents(
    		@PathVariable Long id, @RequestBody List<String> torrentUrls) throws TorrentClientException {

    	List<TorrentDto> addedTorrents = feedProviderService.addTorrent(id, torrentUrls);

        return new ResponseEntity<List<TorrentDto>>(addedTorrents, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/feedProviders/{id}/torrents/{torrentStateId}/download", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> downloadFeedProviderTorrent(@PathVariable Long id, @PathVariable Long torrentStateId) {
    	boolean downloading = false;
    	
		try {
			downloading = feedProviderService.downloadTorrent(id, torrentStateId);
		} catch (TorrentClientException e) {
        	return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
    	
    	if (!downloading) {
    		// torrent could not be added because it's already downloaded or completed downloading
        	return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    	}
    	
    	return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @RequestMapping(value = "/feedProviders/{id}/filterAttributes", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<List<FilterAttributeDto>> replaceFeedProviderAttributes(
    		@PathVariable Long id, @RequestBody List<FilterAttributeDto> filterAttributeDtos) {
    	List<FilterAttributeDto> replacedFilterAttributes = feedProviderService.replaceFeedProviderFilterAttributes(id, filterAttributeDtos);
        return new ResponseEntity<List<FilterAttributeDto>>(replacedFilterAttributes, HttpStatus.CREATED);
    }

    @GetMapping("/feedProviders/{id}/filterAttributes")
    @ResponseBody
    public ResponseEntity<List<FilterAttributeDto>> getFeedProviderFilterAttributes(@PathVariable Long id) {
    	List<FilterAttributeDto> filterAttributes = feedProviderService.getFeedProviderFilterAttributes(id);
        return new ResponseEntity<List<FilterAttributeDto>>(filterAttributes, HttpStatus.OK);
    }
    
    
}
