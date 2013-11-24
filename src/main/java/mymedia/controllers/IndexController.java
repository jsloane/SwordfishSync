package mymedia.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import mymedia.db.form.FeedInfo;
import mymedia.db.form.FilterAttribute;
import mymedia.db.form.TorrentInfo;
import mymedia.model.UploadedFile;
import mymedia.services.MediaManager;
import mymedia.services.model.FeedProvider;

import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.thoughtworks.xstream.XStream;

@Controller
public class IndexController {
	
    @RequestMapping("/")
    public ModelAndView index() {
    	// ALLOW CUSTOM JSP DEFINED BY PROPERTY, or uploadable
		
		String message = "MyMedia IndexController";
		
		
		// list feeds and torrents
		ModelAndView mav = new ModelAndView("index");
		mav.addObject("message", message);
		mav.addObject("feeds", MediaManager.feedProviders);
		
		// copy into new list to sort??
		
		// list all torrents from client and sort by activity date
		
        return mav;
    }
    @RequestMapping("/active-torrents")
    public ModelAndView activeTorrents() {
		String message = "MyMedia Active Torrents";
		
		
		// list feeds and torrents
		ModelAndView mav = new ModelAndView("activeTorrents");
		mav.addObject("message", message);
		
		try {
			mav.addObject("activeTorrents", MediaManager.getAllTorrentStatus());
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
        return mav;
    }
    
    @RequestMapping(value = "/feeds", method = RequestMethod.GET)
    public ModelAndView feeds() {
    //public String getFeed(Map<String, Object> map, @PathVariable("feedId") Integer feedId) {
        //map.put("type", typeService.getType(typeId));


    	// put this in a "getFeedsView" method
		String message = "MyMedia Feeds list";
		
		
		// trigger now
		//MediaManager.syncTorrents(true);
		// force sync rss feeds
		
		
		// list feeds and torrents
		ModelAndView mav = new ModelAndView("feeds");
		mav.addObject("message", message);
		mav.addObject("feeds", MediaManager.feedProviders);
    	
        return mav;
    }
    
    @RequestMapping(value = "/feeds/add", method = RequestMethod.GET)
    public ModelAndView editFeed(@ModelAttribute("uploadedFile") UploadedFile uploadedFile) {
    	
		ModelAndView mav = new ModelAndView("feedEdit");
		FeedProvider newFeedProvider = new FeedProvider();
    	mav.addObject("feed", newFeedProvider); // new feed object for default values
		setFeedCheckboxValues(newFeedProvider, mav);
    	//mav.addObject("feedInfo", new FeedProvider().getFeedInfo()); // new feed object for default values
		mav.addObject("message", "MyMedia new feed");
		mav.addObject("newFeed", true);
		//mav.addObject("newFeed", true); // needed for initialPopulate option?
		//mav.addObject("newLine", System.lineSeparator());
		
		
        return mav;
    }
    
	@RequestMapping(value = "/feeds/add", method = RequestMethod.POST)
    //public String saveNewFeed(@ModelAttribute("feedProvider") FeedProvider feedprovider, BindingResult result) {
    public String saveNewFeed(WebRequest webRequest) {
    	FeedProvider newFeedProvider = new FeedProvider(); // don't pass in url, it could replace the feed data if it already exists
    	boolean saved = saveFeedInfoValues(newFeedProvider, webRequest);
    	if (saved) {
    		MediaManager.feedProviders.add(newFeedProvider);
            return "redirect:/feeds/" + newFeedProvider.getFeedInfo().getId();
    	}
		
        return "redirect:/feeds/add";
    }
    
    @RequestMapping(value = "/feeds/add/upload", method = RequestMethod.POST)
    public String saveNewFeedUpload(@ModelAttribute("uploadedFile") UploadedFile uploadedFile) throws IOException {
    	// http://www.beingjavaguys.com/2013/08/spring-mvc-file-upload-example.html
    	
    	uploadedFile.getFile().getInputStream();
    	
		XStream xstream = new XStream();
		xstream.autodetectAnnotations(true);
		xstream.alias("feedInfo", FeedInfo.class);
		FeedProvider uploadedFeed = new FeedProvider(
				(FeedInfo) xstream.fromXML(uploadedFile.getFile().getInputStream())
		);
		
		if (uploadedFeed.getFeedInfo().getUrl() != null && uploadedFeed.getFeedInfo().getName() != null) {
	    	uploadedFeed.saveFeedInfo();
	    	MediaManager.feedProviders.add(uploadedFeed);
	    	return "redirect:/feeds/" + uploadedFeed.getFeedInfo().getId();
		}
		
		return "redirect:/feeds/add"; // needs error message/validation
    }
    
    
    @RequestMapping("/feeds/{feedId}")
    public ModelAndView getFeed(@PathVariable("feedId") Integer feedId) {
		String message = "MyMedia feed details";
		ModelAndView mav = new ModelAndView("feed");
    	mav.addObject("feed", findFeedProviders(new Integer[]{feedId}).get(0));
		mav.addObject("torrentNotAdded", TorrentInfo.STATUS_NOT_ADDED);
		mav.addObject("torrentNotifiedNotAdded", TorrentInfo.STATUS_NOTIFIED_NOT_ADDED);
		mav.addObject("torrentSkipped", TorrentInfo.STATUS_SKIPPED);
		mav.addObject("message", message);
		mav.addObject("message", message);
		
		
        return mav;
    }
    
	@RequestMapping(value = "/feeds/{feedId}/export", method = RequestMethod.GET)
    public ModelAndView exportFeed(@PathVariable("feedId") Integer feedId, HttpServletResponse response) throws IOException {
		
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);
		
		String message = "MyMedia feed details";
		ModelAndView mav = new ModelAndView("feed");
    	mav.addObject("feed", foundFeedProvider);
		mav.addObject("message", message);
		
		/*
		// convert string array to integer array
    	Integer[] selectedFeedsIds = new Integer[selectedFeeds.length];
        int i=0;
        for(String selectedFeed : selectedFeeds) {
        	selectedFeedsIds[i] = Integer.parseInt(selectedFeed.trim());
            i++;
        }
        */
    	
    	/*System.out.println("[DEBUG] selectedFeeds: " + selectedFeeds);
    	for (String s : selectedFeeds) {
        	System.out.println("[DEBUG] selected feed id: " + s);
    	}*/
    	
    	if (foundFeedProvider != null) {
        	// export XML
    		XStream xstream = new XStream();
    		xstream.autodetectAnnotations(true);
    		xstream.alias("feedInfo", FeedInfo.class);
    		
    		//StringBuffer xml = new StringBuffer();
    		//xml.append(xstream.toXML(foundFeedProvider.getFeedInfo()));
    		//byte[] b = xml.toString().getBytes();
    		byte[] b = xstream.toXML(foundFeedProvider.getFeedInfo()).getBytes();
    		response.setHeader("Pragma", "private");
    		response.setHeader("Cache-Control", "private, must-revalidate");
    		response.setHeader("Content-Disposition","attachment; filename=\"" + foundFeedProvider.getFeedInfo().getName() +".xml\"");
    		response.setContentType("text/xml");
    		response.setContentLength(b.length);
    		ServletOutputStream ouputStream = response.getOutputStream();
    		ouputStream.write(b);
    		ouputStream.flush();
    		ouputStream.close(); 		
    		
    		return null;
    	}
    	
    	//return "redirect:/feeds";
    	return mav;
    }
    
    @RequestMapping("/feeds/{feedId}/torrent/{torrentId}/download")
    public String downloadTorrent(@PathVariable("feedId") Integer feedId, @PathVariable("torrentId") Integer torrentId) {
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);
    	TorrentInfo foundTorrentInfo = null;
    	
    	for (TorrentInfo torrentInfo : foundFeedProvider.getFeedInfo().getFeedTorrents()) {
	    	if (torrentInfo.getId().equals(torrentId)) {
	    		foundTorrentInfo = torrentInfo;
	    		break;
	    	}
    	}
    	
    	if (foundTorrentInfo != null && (
    			foundTorrentInfo.getStatus().equals(TorrentInfo.STATUS_NOT_ADDED) ||
    			foundTorrentInfo.getStatus().equals(TorrentInfo.STATUS_NOTIFIED_NOT_ADDED) ||
    			foundTorrentInfo.getStatus().equals(TorrentInfo.STATUS_SKIPPED)
    		)) {
    		MediaManager.addTorrent(foundFeedProvider, foundTorrentInfo);
    	}
    	
        return "redirect:/feeds/" + feedId;
    }
    
    @RequestMapping(value = "/feeds/{feedId}/edit", method = RequestMethod.GET)
    public ModelAndView editFeed(@PathVariable("feedId") Integer feedId) {
		ModelAndView mav = new ModelAndView("feedEdit");
		
    	for (FeedProvider feed : MediaManager.feedProviders) {
    		if (feed.getFeedInfo().getId().equals(feedId)) {
    			mav.addObject("feed", feed);
    			setFeedCheckboxValues(feed, mav);
    			break;
    		}
    	}
		mav.addObject("message", "MyMedia edit feed");
		mav.addObject("newFeed", false);
		
        return mav;
    }
    @RequestMapping(value = "/feeds/{feedId}/edit", method = RequestMethod.POST)
    public String saveFeed(@PathVariable("feedId") Integer feedId, WebRequest webRequest) {
    	
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);
    	/*
    	// need to fix concurrent editing of feed. SyncTask should load feeds on every run, and only save torrents, not feeds? if it becomes a problem
    	System.out.println("[DEBUG] got feed imfo: " + MediaManager.feedInfoService.getFeedInfo(feedId));
    	FeedProvider foundFeedProvider = new FeedProvider(MediaManager.feedInfoService.getFeedInfo(feedId));
    	System.out.println("[DEBUG] got feed provider: " + foundFeedProvider.getName());
    	*/
    	//System.out.println("[DEBUG] edited name: " + webRequest.getParameter("feed_name"));
    	
    	if (foundFeedProvider != null && !foundFeedProvider.getFromPropertiesFile()) {
        	// save data
	    	saveFeedInfoValues(foundFeedProvider, webRequest);
    	}
    	
        return "redirect:/feeds/" + feedId;
    }
    
	@RequestMapping(value = "/feeds/{feedId}/edit/filter", method = RequestMethod.GET)
    public ModelAndView editFeedFilter(@PathVariable("feedId") Integer feedId) {
		String optionSelected = "selected=\"selected\"";
		ModelAndView mav = new ModelAndView("feedEditFilter");
    	String filterEnabled = "";
    	String actionSelectedIgnore = optionSelected;
    	String actionSelectedAdd = "";
    	String precedenceSelectedIgnore = optionSelected;
    	String precedenceSelectedAdd = "";
    	for (FeedProvider feed : MediaManager.feedProviders) {
    		if (feed.getFeedInfo().getId().equals(feedId)) {
    			mav.addObject("feed", feed);
    			if (feed.getFeedInfo().getFilterEnabled()) {
    				filterEnabled = "checked=\"checked\"";
    			}
    			if ("add".equals(feed.getFeedInfo().getFilterAction())) {
    				actionSelectedIgnore = "";
    				actionSelectedAdd = optionSelected;
    			}
    			if ("add".equals(feed.getFeedInfo().getFilterPrecedence())) {
    				precedenceSelectedIgnore = "";
    				precedenceSelectedAdd = optionSelected;
    			}
    			break;
    		}
    	}

		mav.addObject("filterEnabled", filterEnabled);
		mav.addObject("actionSelectedIgnore", actionSelectedIgnore);
		mav.addObject("actionSelectedAdd", actionSelectedAdd);
		mav.addObject("precedenceSelectedIgnore", precedenceSelectedIgnore);
		mav.addObject("precedenceSelectedAdd", precedenceSelectedAdd);
		mav.addObject("message", "MyMedia edit feed filter");
		mav.addObject("newLine", System.lineSeparator());
        return mav;
    }
    
    @RequestMapping(value = "/feeds/{feedId}/edit/filter", method = RequestMethod.POST)
    public String saveFeedFilter(@PathVariable("feedId") Integer feedId, WebRequest webRequest) {
    	
    	//  should be getting feedInfo object from hibernate service, to avoid potential concurrent editing? if it becomes a problem
    	//FeedProvider foundFeedProvider = new FeedProvider(MediaManager.feedInfoService.getFeedInfo(feedId));
    	
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);
    	
    	// filter_enabled
    	if ("on".equals(webRequest.getParameter("filter_enabled"))) {
        	foundFeedProvider.getFeedInfo().setFilterEnabled(true);
    	} else {
        	foundFeedProvider.getFeedInfo().setFilterEnabled(false);
    	}
    	// filter_action
    	String filterAction = webRequest.getParameter("filter_action");
    	if ("add".equals(filterAction) || "ignore".equals(filterAction)) {
        	foundFeedProvider.getFeedInfo().setFilterAction(filterAction);
    	}
    	// filter_precedence
    	String filterPrecedence = webRequest.getParameter("filter_precedence");
    	if ("add".equals(filterPrecedence) || "ignore".equals(filterPrecedence)) {
        	foundFeedProvider.getFeedInfo().setFilterPrecedence(filterPrecedence);
    	}
    	
    	
    	// need option for setRemoveAddFilterOnMatch - rename to setFilterRemoveAddOnMatch
    	
    	
    	// filter entries
    	if (foundFeedProvider != null) {
        	String eol = System.lineSeparator();
    	    Set<FilterAttribute> filterAttributes = foundFeedProvider.getFeedInfo().getFilterAttributes();
    	    filterAttributes.clear();
	    	for (String regex : webRequest.getParameter("filter_add_regex").split(eol)) {
	    		if (!regex.isEmpty() && regex.trim().length() > 0) {
	    			filterAttributes.add(
	        			new FilterAttribute("add", regex.trim())
	        		);
	    		}
	    	}
	    	for (String regex : webRequest.getParameter("filter_ignore_regex").split(eol)) {
	    		if (!regex.isEmpty() && regex.trim().length() > 0) {
	    			filterAttributes.add(
	        			new FilterAttribute("ignore", regex.trim())
	        		);
	    		}
	    	}
	    	foundFeedProvider.getFeedInfo().setFilterAttributes(filterAttributes);
	    	foundFeedProvider.saveFeedInfo();
    	}
        return "redirect:/feeds/" + feedId;
    }
    
    
    
    // delete feed
    // warn if feed has active torrents, they will not be modified/stopped/deleted from torrent client, no longer managed by mymedia
	@RequestMapping(value = "/feeds/{feedId}/delete", method = RequestMethod.GET)
    public ModelAndView deleteFeed(@PathVariable("feedId") Integer feedId) {
		ModelAndView mav = new ModelAndView("feedDelete");
    	for (FeedProvider feed : MediaManager.feedProviders) {
    		if (feed.getFeedInfo().getId().equals(feedId)) {
    			mav.addObject("feed", feed);
    			break;
    		}
    	}
		mav.addObject("message", "MyMedia delete feed - not implemented");
		// Warning: The listed torrents are currently downloading, and will no longer be managed my MyMedia
		// list torrents IN_PROGRESS, other status?
		// confirm button, cancel button
        return mav;
    }
    
    @RequestMapping(value = "/feeds/{feedId}/delete", method = RequestMethod.POST)
    public String deleteFeed(@PathVariable("feedId") Integer feedId, WebRequest webRequest) {
    	
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);
    	
    	if (foundFeedProvider != null && !foundFeedProvider.getFromPropertiesFile()) {
    		
    		
    	}
        return "redirect:/feeds";
    }
    
    
    private void setFeedCheckboxValues(FeedProvider feedProvider, ModelAndView mav) {
		String optionChecked = "checked=\"checked\"";
    	String checkedInitialPopulate = "";
    	String checkedRemoveTorrentOnComplete = "";
    	String checkedExtractRars = "";
    	String checkedDetermineSubDirectory = "";
    	
		if (feedProvider.getFeedInfo().getInitialPopulate()) {
			checkedInitialPopulate = optionChecked;
		}
		if (feedProvider.getFeedInfo().getRemoveTorrentOnComplete()) {
			checkedRemoveTorrentOnComplete = optionChecked;
		}
		if (feedProvider.getFeedInfo().getExtractRars()) {
			checkedExtractRars = optionChecked;
		}
		if (feedProvider.getFeedInfo().getDetermineSubDirectory()) {
			checkedDetermineSubDirectory = optionChecked;
		}
		
		mav.addObject("checkedInitialPopulate", checkedInitialPopulate);
		mav.addObject("checkedRemoveTorrentOnComplete", checkedRemoveTorrentOnComplete);
		mav.addObject("checkedExtractRars", checkedExtractRars);
		mav.addObject("checkedDetermineSubDirectory", checkedDetermineSubDirectory);
	}
    
    private boolean saveFeedInfoValues(FeedProvider feedProvider, WebRequest webRequest) {
		String url = webRequest.getParameter("feed_url").trim();
		String name = webRequest.getParameter("feed_name").trim();
		if (url != null && !url.isEmpty() && name != null && !name.isEmpty()) { // need better validation/errors
			
			// need to validate these fields
			/**
			 * 
			##### FEED PROPERTIES ######
			# feed properties
			# isActive - still fetches feed, but does not add torrents
			# syncInterval in minutes
			# action: add/nofify/ignore
			# uploadLimit in Kbps
			# activityInterval in days
			# deleteInterval in days
			# action: download/notify/ignore
			# downloadDirectory only use "/" in directory path
			# filter.action: add/ignore (default ignore)
			# removeTorrentOnComplete: remove torrent from torrent client
			# initialPopulate: add all existing torrents when feed first added
			 *
			 */
			
			
	    	feedProvider.getFeedInfo().setUrl(url);
	    	feedProvider.getFeedInfo().setName(name);
	    	feedProvider.getFeedInfo().setSyncInterval(Integer.parseInt(webRequest.getParameter("feed_syncInterval").trim()));
	    	feedProvider.getFeedInfo().setDeleteInterval(Integer.parseInt(webRequest.getParameter("feed_deleteInterval").trim()));
	    	feedProvider.getFeedInfo().setDownloadDirectory(webRequest.getParameter("feed_downloadDirectory").trim());
	    	feedProvider.getFeedInfo().setUploadLimit(Integer.parseInt(webRequest.getParameter("feed_uploadLimit").trim()));
	    	feedProvider.getFeedInfo().setNotifyEmail(webRequest.getParameter("feed_notifyEmail").trim());
	    	
	    	// other fields
	    	if ("on".equals(webRequest.getParameter("feed_initialPopulate"))) {
	    		feedProvider.getFeedInfo().setInitialPopulate(true);
	    	} else {
	    		feedProvider.getFeedInfo().setInitialPopulate(false);
	    	}
	    	if ("on".equals(webRequest.getParameter("feed_removeTorrentOnComplete"))) {
	    		feedProvider.getFeedInfo().setRemoveTorrentOnComplete(true);
	    	} else {
	    		feedProvider.getFeedInfo().setRemoveTorrentOnComplete(false);
	    	}
	    	if ("on".equals(webRequest.getParameter("feed_extractRars"))) {
	    		feedProvider.getFeedInfo().setExtractRars(true);
	    	} else {
	    		feedProvider.getFeedInfo().setExtractRars(false);
	    	}
	    	if ("on".equals(webRequest.getParameter("feed_determineSubDirectory"))) {
	    		feedProvider.getFeedInfo().setDetermineSubDirectory(true);
	    	} else {
	    		feedProvider.getFeedInfo().setDetermineSubDirectory(false);
	    	}
	    	
	    	feedProvider.getFeedInfo().setUpdated(new Date());
	    	feedProvider.saveFeedInfo();
	    	return true;
		}
    	return false;
	}
    
    private List<FeedProvider> findFeedProviders(Integer[] feedIds) {
    	List<FeedProvider> foundFeedProviders = new ArrayList<FeedProvider>();
    	for (FeedProvider feed : MediaManager.feedProviders) {
    		for (Integer feedId : feedIds) {
	    		if (feed.getFeedInfo().getId().equals(feedId)) {
	    			foundFeedProviders.add(feed);
	    			break;
	    		}
    		}
    	}
    	return foundFeedProviders;
    }
    
}