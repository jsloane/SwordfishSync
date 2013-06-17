package mymedia.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mymedia.db.form.FilterAttribute;
import mymedia.services.MediaManager;
import mymedia.services.model.FeedProvider;

import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

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
    
    @RequestMapping("/feeds")
    public ModelAndView feeds() {
    //public String getFeed(Map<String, Object> map, @PathVariable("feedId") Integer feedId) {
        //map.put("type", typeService.getType(typeId));

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
    
    @RequestMapping("/feeds/{feedId}")
    public ModelAndView getFeed(@PathVariable("feedId") Integer feedId) {
		String message = "MyMedia feed details";
		ModelAndView mav = new ModelAndView("feed");
    	for (FeedProvider feed : MediaManager.feedProviders) {
    		if (feed.getFeedInfo().getId().equals(feedId)) {
    			mav.addObject("feed", feed);
    		}
    	}
		mav.addObject("message", message);

        return mav;
    }
    
    @RequestMapping("/feeds/{feedId}/download/{torrentId}")
    public String downloadTorrent(@PathVariable("feedId") Integer feedId, @PathVariable("torrentId") Integer torrentId) {
    	System.out.println("[DEBUG] downloadTorrent");
    	System.out.println("[DEBUG] feedId: " + feedId);
    	System.out.println("[DEBUG] torrentId: " + torrentId);
    	
    	
        return "redirect:/feed/" + feedId;
    }
    
    @RequestMapping(value = "/feeds/add", method = RequestMethod.GET)
    public ModelAndView editFeed() {
    	
		ModelAndView mav = new ModelAndView("feedEdit");
    	mav.addObject("feed", new FeedProvider()); // new feed object for default values
		mav.addObject("message", "MyMedia new feed");
		//mav.addObject("newFeed", true); // needed for initialPopulate option?
		//mav.addObject("newLine", System.lineSeparator());
		
        return mav;
    }
    
    @RequestMapping(value = "/feeds/add", method = RequestMethod.POST)
    //public String saveNewFeed(@ModelAttribute("feedProvider") FeedProvider feedprovider, BindingResult result) {
    public String saveNewFeed(WebRequest webRequest) {
    	System.out.println("[DEBUG] SAVE NEW FEED POST");
    	
    	// save new feed
    	
    	// check URL first, then proceed
    	// save feed info in common method
    	
    	//OPTION TO DLOWNLOAD ALL EXISTING TORRENTS OR NOT - checkbox
    	
		String url = webRequest.getParameter("feed_url").trim();
		if (url != null && !url.isEmpty()) {
        	FeedProvider newFeedProvider = new FeedProvider(); // don't pass in url, it could replace the feed data if it already exists
    		saveFeedInfoValues(newFeedProvider, webRequest);
    		
    		MediaManager.feedProviders.add(newFeedProvider);
    		
            return "redirect:/feeds/" + newFeedProvider.getFeedInfo().getId();
		}
		
        return "redirect:/feeds/add";
		
    }
    
    @RequestMapping(value = "/feeds/{feedId}/edit", method = RequestMethod.GET)
    public ModelAndView editFeed(@PathVariable("feedId") Integer feedId) {

		ModelAndView mav = new ModelAndView("feedEdit");

    	for (FeedProvider feed : MediaManager.feedProviders) {
    		if (feed.getFeedInfo().getId().equals(feedId)) {
    			mav.addObject("feed", feed);
    		}
    	}
		mav.addObject("message", "MyMedia edit feed");
		
        return mav;
    }
    @RequestMapping(value = "/feeds/{feedId}/edit", method = RequestMethod.POST)
    public String saveFeed(@PathVariable("feedId") Integer feedId, WebRequest webRequest) {
    	
    	FeedProvider foundFeedProvider = findFeedProvider(feedId);
    	/*
    	// need to fix concurrent editing of feed. SyncTask should load feeds on every run, and only save torrents, not feeds? if it becomes a problem
    	System.out.println("[DEBUG] got feed imfo: " + MediaManager.feedInfoService.getFeedInfo(feedId));
    	FeedProvider foundFeedProvider = new FeedProvider(MediaManager.feedInfoService.getFeedInfo(feedId));
    	System.out.println("[DEBUG] got feed provider: " + foundFeedProvider.getName());
    	*/
    	
    	if (foundFeedProvider != null) {
        	// save all data, and trim values
    		String url = webRequest.getParameter("feed_url").trim();
    		if (url != null && !url.isEmpty()) {
	    		saveFeedInfoValues(foundFeedProvider, webRequest);
    		}
    	}
    	
        return "redirect:/feeds/" + feedId;
    }
    
    private void saveFeedInfoValues(FeedProvider feedProvider, WebRequest webRequest) {
    	feedProvider.getFeedInfo().setUrl(webRequest.getParameter("feed_url").trim());
    	feedProvider.getFeedInfo().setName(webRequest.getParameter("feed_name").trim());
    	feedProvider.saveFeedInfo();
	}
    
    private FeedProvider findFeedProvider(int feedId) {
    	FeedProvider foundFeedProvider = null;
    	for (FeedProvider feed : MediaManager.feedProviders) {
    		if (feed.getFeedInfo().getId().equals(feedId)) {
    			foundFeedProvider = feed;
    		}
    	}
    	return foundFeedProvider;
    }
    
	@RequestMapping(value = "/feeds/{feedId}/edit/filter", method = RequestMethod.GET)
    public ModelAndView editFeedFilter(@PathVariable("feedId") Integer feedId) {
		ModelAndView mav = new ModelAndView("feedEditFilter");
    	for (FeedProvider feed : MediaManager.feedProviders) {
    		if (feed.getFeedInfo().getId().equals(feedId)) {
    			mav.addObject("feed", feed);
    		}
    	}
		mav.addObject("message", "MyMedia edit feed filter");
		mav.addObject("newLine", System.lineSeparator());
        return mav;
    }
    
    @RequestMapping(value = "/feeds/{feedId}/edit/filter", method = RequestMethod.POST)
    public String saveFeedFilter(@PathVariable("feedId") Integer feedId, WebRequest webRequest) {
    	
    	//  should be getting feedInfo object from hibernate service, to avoid potential concurrent editing? if it becomes a problem
    	//FeedProvider foundFeedProvider = new FeedProvider(MediaManager.feedInfoService.getFeedInfo(feedId));
    	
    	FeedProvider foundFeedProvider = findFeedProvider(feedId);
    	
    	if (foundFeedProvider != null) {
        	String eol = System.lineSeparator();
    	    Set<FilterAttribute> filterAttributes = new HashSet<FilterAttribute>();
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
    // warn if feed has active torrents

}