package mymedia.controllers;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mymedia.db.form.FeedInfo;
import mymedia.db.form.FilterAttribute;
import mymedia.db.form.TorrentInfo;
import mymedia.model.UploadedFile;
import mymedia.services.MediaManager;
import mymedia.services.MyMediaLifecycle;
import mymedia.services.model.FeedProvider;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ca.benow.transmission.model.AddedTorrentInfo;

import com.thoughtworks.xstream.XStream;

@Controller
public class IndexController {
	
	public static String instanceName = "";
	
    @RequestMapping("/")
    public ModelAndView index() throws IOException {
		ModelAndView mav;
		
    	// Check for custom JSP file
    	Resource customIndex = new ClassPathResource("index.jsp");
    	if (customIndex.exists() && customIndex.isReadable()) {
    		mav = setCommonObjects(new ModelAndView("classes/index"));
    	} else {
    		mav = setCommonObjects(new ModelAndView("jsp/index"));
    	}
		
		mav.addObject("feeds", MediaManager.feedProviders);
		mav.addObject("torrentDownloading", TorrentInfo.STATUS_IN_PROGRESS);
		mav.addObject("torrentNotifiedCompleted", TorrentInfo.STATUS_NOTIFY_COMPLETED);
		mav.addObject("torrentCompleted", TorrentInfo.STATUS_COMPLETED);
		mav.addObject("torrentNotifiedNotAdded", TorrentInfo.STATUS_NOTIFIED_NOT_ADDED);
		
        return mav;
    }
    
    @RequestMapping("/client-torrents")
    public ModelAndView activeTorrents() {
		// list feeds and torrents from torrent client
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/clientTorrents"));
		mav.addObject("activeTorrents", MediaManager.getAllTorrentStatus());
        return mav;
    }
    
    @RequestMapping(value = "/feeds", method = RequestMethod.GET)
    public ModelAndView feeds() {
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/feeds"));
		mav.addObject("feeds", MediaManager.feedProviders);
        return mav;
    }
    
    @RequestMapping(value = "/feeds/add", method = RequestMethod.GET)
    public ModelAndView editFeed(@ModelAttribute("uploadedFile") UploadedFile uploadedFile) {
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/feedEdit"));
		FeedProvider newFeedProvider = new FeedProvider();
    	mav.addObject("feed", newFeedProvider); // new feed object for default values
		setFeedValues(newFeedProvider, mav);
		mav.addObject("newFeed", true);
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
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/feed"));
    	mav.addObject("feed", findFeedProviders(new Integer[]{feedId}).get(0));
		mav.addObject("torrentNotAdded", TorrentInfo.STATUS_NOT_ADDED);
		mav.addObject("torrentNotifiedNotAdded", TorrentInfo.STATUS_NOTIFIED_NOT_ADDED);
		mav.addObject("torrentSkipped", TorrentInfo.STATUS_SKIPPED);
        return mav;
    }
    
	@RequestMapping(value = "/feeds/{feedId}/export", method = RequestMethod.GET)
    public ModelAndView exportFeed(@PathVariable("feedId") Integer feedId, HttpServletResponse response) throws IOException {
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);
		
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/feed"));
    	mav.addObject("feed", foundFeedProvider);
    	
    	if (foundFeedProvider != null) {
        	// export XML
    		XStream xstream = new XStream();
    		xstream.autodetectAnnotations(true);
    		xstream.alias("feedInfo", FeedInfo.class);
    		
    		byte[] b = xstream.toXML(foundFeedProvider.getFeedInfo()).getBytes();
    		response.setHeader("Pragma", "private");
    		response.setHeader("Cache-Control", "private, must-revalidate");
    		response.setHeader("Content-Disposition","attachment; filename=\"" + IndexController.instanceName + " - " + foundFeedProvider.getFeedInfo().getName() +".xml\"");
    		response.setContentType("text/xml");
    		response.setContentLength(b.length);
    		ServletOutputStream ouputStream = response.getOutputStream();
    		ouputStream.write(b);
    		ouputStream.flush();
    		ouputStream.close();
    		return null;
    	}
    	
    	return mav;
    }
	
    @RequestMapping(value = "/feeds/{feedId}/torrents/add", method = RequestMethod.GET)
    public ModelAndView addTorrent(@PathVariable("feedId") Integer feedId) {
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/torrentEdit"));
		mav.addObject("feed", findFeedProviders(new Integer[]{feedId}).get(0));
		TorrentInfo newTorrentInfo = new TorrentInfo();
    	mav.addObject("torrentInfo", newTorrentInfo); // new feed object for default values
		mav.addObject("newTorrent", true);
		
        return mav;
    }
	@RequestMapping(value = "/feeds/{feedId}/torrents/add", method = RequestMethod.POST)
    public String saveNewTorrent(@PathVariable("feedId") Integer feedId, WebRequest webRequest, RedirectAttributes redir) throws InterruptedException {
		// this will overwrite existing torrent if exists with the url
		
		List<String> addedTorrents = new ArrayList<String>();
		List<String> erroredTorrents = new ArrayList<String>();
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);
		String torrentUrls = webRequest.getParameter("torrent_urls").trim();
		
		if (foundFeedProvider != null) {
			for (String torrentUrl : torrentUrls.split("\\n")) { // need better validation/errors
				torrentUrl = torrentUrl.trim();
				if (StringUtils.isNotBlank(torrentUrl)) {
		        	TorrentInfo newTorrent = new TorrentInfo(
		        		"Torrent manually added at " + new Date(),
						torrentUrl,
						new Date(),
						null,
						TorrentInfo.STATUS_NOTIFIED_NOT_ADDED
		    		);
		        	
		        	AddedTorrentInfo ati = null;
					try {
						ati = MediaManager.addTorrent(foundFeedProvider, newTorrent);
						if (ati != null) {
							newTorrent.setName(ati.getName());
							foundFeedProvider.saveTorrent(newTorrent);
							addedTorrents.add(newTorrent.getName());
						}
					} catch (IOException e) {
						e.printStackTrace();
						erroredTorrents.add(torrentUrl + ". Error: " + e.getMessage());
					}
				} else {
					erroredTorrents.add("Blank URL entered.");
				}
			}
			
			// set success/error messages
			if (!addedTorrents.isEmpty()) {
				List<String> successMessages = new ArrayList<String>();
				StringBuilder successMessage = new StringBuilder();
				for (String message : addedTorrents) {
					successMessage.append(message + "<br/>");
				}
				successMessages.add("Successfully added torrent(s):<br/>" + successMessage.toString());
				redir.addFlashAttribute("successMessages", successMessages);
			}
			if (!erroredTorrents.isEmpty()) {
				List<String> erroredMessages = new ArrayList<String>();
				StringBuilder errorMessage = new StringBuilder();
				for (String message : erroredTorrents) {
					errorMessage.append(message + "<br/>");
				}
				erroredMessages.add("Error adding torrent(s):<br/>" + errorMessage.toString());
				redir.addFlashAttribute("errorMessages", erroredMessages);
			}
			
			if (!addedTorrents.isEmpty()) {
				return "redirect:/feeds/" + feedId;
			}
		}
		
        return "redirect:/feeds/" + feedId + "/torrents/add";
    }
    
    @RequestMapping("/feeds/{feedId}/torrents/{torrentId}/download")
    public String downloadTorrent(@PathVariable("feedId") Integer feedId, @PathVariable("torrentId") Integer torrentId, RedirectAttributes redir) {
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);
    	TorrentInfo foundTorrentInfo = null;
    	
    	for (TorrentInfo torrentInfo : foundFeedProvider.getFeedInfo().getFeedTorrents()) {
	    	if (torrentInfo != null && torrentInfo.getId().equals(torrentId)) {
	    		foundTorrentInfo = torrentInfo;
	    		break;
	    	}
    	}
    	
    	if (foundTorrentInfo != null && (
    			foundTorrentInfo.getStatus().equals(TorrentInfo.STATUS_NOT_ADDED) ||
    			foundTorrentInfo.getStatus().equals(TorrentInfo.STATUS_NOTIFIED_NOT_ADDED) ||
    			foundTorrentInfo.getStatus().equals(TorrentInfo.STATUS_SKIPPED)
    		)) {
    		try {
    			MediaManager.addTorrent(foundFeedProvider, foundTorrentInfo);
				List<String> successMessages = new ArrayList<String>();
				successMessages.add("Downloading torrent: " + foundTorrentInfo.getName());
				redir.addFlashAttribute("successMessages", successMessages);
    		} catch (IOException e) {
    			e.printStackTrace();
				List<String> erroredMessages = new ArrayList<String>();
				erroredMessages.add("Error adding torrent: " + ExceptionUtils.getMessage(e) + ". Cause: " + ExceptionUtils.getRootCauseMessage(e));
				redir.addFlashAttribute("errorMessages", erroredMessages);
    		}
    	}
    	
        return "redirect:/feeds/" + feedId;
    }
    
    @RequestMapping(value = "/feeds/{feedId}/edit", method = RequestMethod.GET)
    public ModelAndView editFeed(@PathVariable("feedId") Integer feedId) {
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/feedEdit"));
		
    	for (FeedProvider feed : MediaManager.feedProviders) {
    		if (feed.getFeedInfo().getId().equals(feedId)) {
    			mav.addObject("feed", feed);
    			setFeedValues(feed, mav);
    			break;
    		}
    	}
		mav.addObject("newFeed", false);
		
        return mav;
    }
    @RequestMapping(value = "/feeds/{feedId}/edit", method = RequestMethod.POST)
    public String saveFeed(@PathVariable("feedId") Integer feedId, WebRequest webRequest) {
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);
    	if (foundFeedProvider != null) {
        	// save data
	    	saveFeedInfoValues(foundFeedProvider, webRequest);
    	}
        return "redirect:/feeds/" + feedId;
    }
    
	@RequestMapping(value = "/feeds/{feedId}/edit/filter", method = RequestMethod.GET)
    public ModelAndView editFeedFilter(@PathVariable("feedId") Integer feedId) {
		String optionSelected = "selected=\"selected\"";
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/feedEditFilter"));
    	String filterEnabled = "";
    	String removeAddFilterOnMatch = "";
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
    			if (feed.getFeedInfo().getRemoveAddFilterOnMatch()) {
    				removeAddFilterOnMatch = "checked=\"checked\"";
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
		mav.addObject("removeAddFilterOnMatch", removeAddFilterOnMatch);
		mav.addObject("actionSelectedIgnore", actionSelectedIgnore);
		mav.addObject("actionSelectedAdd", actionSelectedAdd);
		mav.addObject("precedenceSelectedIgnore", precedenceSelectedIgnore);
		mav.addObject("precedenceSelectedAdd", precedenceSelectedAdd);
		mav.addObject("newLine", System.lineSeparator());
        return mav;
    }
    @RequestMapping(value = "/feeds/{feedId}/edit/filter", method = RequestMethod.POST)
    public String saveFeedFilter(@PathVariable("feedId") Integer feedId, WebRequest webRequest) {
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);

    	// filter_enabled
    	if ("on".equals(webRequest.getParameter("filter_enabled"))) {
        	foundFeedProvider.getFeedInfo().setFilterEnabled(true);
    	} else {
        	foundFeedProvider.getFeedInfo().setFilterEnabled(false);
    	}
    	// filter_removeAddFilterOnMatch
    	if ("on".equals(webRequest.getParameter("filter_removeAddFilterOnMatch"))) {
        	foundFeedProvider.getFeedInfo().setRemoveAddFilterOnMatch(true);
    	} else {
        	foundFeedProvider.getFeedInfo().setRemoveAddFilterOnMatch(false);
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
    
	@RequestMapping(value = "/feeds/{feedId}/delete", method = RequestMethod.GET)
    public ModelAndView deleteFeed(@PathVariable("feedId") Integer feedId) {
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/feedDelete"));
    	for (FeedProvider feed : MediaManager.feedProviders) {
    		if (feed.getFeedInfo().getId().equals(feedId)) {
    			mav.addObject("feed", feed);
    			break;
    		}
    	}
        return mav;
    }
    @RequestMapping(value = "/feeds/{feedId}/delete", method = RequestMethod.POST)
    public String deleteFeed(@PathVariable("feedId") Integer feedId, WebRequest webRequest) {
    	FeedProvider foundFeedProvider = findFeedProviders(new Integer[]{feedId}).get(0);
    	if (foundFeedProvider != null) {
    		foundFeedProvider.removeFeedInfo();
    	}
        return "redirect:/feeds";
    }
    
	@RequestMapping(value = "/settings", method = RequestMethod.GET)
    public ModelAndView viewSettings(WebRequest webRequest) throws ConfigurationException {
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/settings"));
		if ("true".equals(webRequest.getParameter("saved"))) {
			String successMessage = "Settings updated.";
			if ("true".equals(webRequest.getParameter("restartRequired"))) {
				successMessage += "You must <a href=\"/manager/text/reload?path=/SwordfishSync\" target=\"_blank\">restart</a> the application for changes to apply.";
			}
			List<String> successMessages = new ArrayList<String>();
			successMessages.add(successMessage);
			mav.addObject("successMessages", successMessages);
		}
		mav.addObject("propertiesFile", MyMediaLifecycle.propertiesFile);
		mav.addObject("configFile", MyMediaLifecycle.configFile);
		XMLConfiguration config = new XMLConfiguration(MyMediaLifecycle.configFile);
		config.setExpressionEngine(new XPathExpressionEngine());
		mav.addObject("config", config);
        return mav;
    }
	@RequestMapping(value = "/settings/edit", method = RequestMethod.GET)
    public ModelAndView editSettings() throws ConfigurationException {
		ModelAndView mav = setCommonObjects(new ModelAndView("jsp/settingsEdit"));
		XMLConfiguration config = new XMLConfiguration(MyMediaLifecycle.configFile);
		config.setExpressionEngine(new XPathExpressionEngine());
		mav.addObject("config", config);
		
        return mav;
    }
    @RequestMapping(value = "/settings/edit", method = RequestMethod.POST)
    public String editSettings(WebRequest webRequest) throws ConfigurationException, IOException {
    	boolean restartRequired = false;
    	boolean saveConfig = false;
    	boolean saveProperties = false;
    	
		XMLConfiguration config = new XMLConfiguration(MyMediaLifecycle.configFile);
		PropertiesConfiguration properties = new PropertiesConfiguration(MyMediaLifecycle.propertiesFile);
		
    	Iterator<String> parameterNames = webRequest.getParameterNames();
    	
    	while (parameterNames.hasNext()) {
    		String parameterName = parameterNames.next();
    		String parameterValue = webRequest.getParameter(parameterName).trim();
    		
    		// save checkbox fields as true
    		if (config.containsKey(parameterName + "[@fieldtype]") && config.getProperty(parameterName + "[@fieldtype]").equals("checkbox")) {
    			if ("on".equals(parameterValue)) {
    				parameterValue = "true";
    			} else {
    				parameterValue = "false";
    			}
    		}
    		
    		if (!parameterValue.equals(config.getProperty(parameterName))) {
            	// update config.xml
        		config.setProperty(parameterName, parameterValue);
        		saveConfig = true;
        	}
        	
    		// update properties only if it contains a value for this key
        	if (properties.containsKey(parameterName) && !properties.getProperty(parameterName).equals(parameterValue)) {
            	// update swordfishsync.properties
        		properties.setProperty(parameterName, parameterValue);
        		saveProperties = true;
        	}
    	}
    	
    	// save changes and reload config
    	if (saveProperties) {
        	restartRequired = true;
        	properties.save();
    	}
    	if (saveConfig) {
    		config.save();
        	MyMediaLifecycle.readConfig();
    	}
    	
        return "redirect:/settings?saved=true&restartRequired=" + Boolean.toString(restartRequired); // needs error message/validation
    }
	@RequestMapping(value = "/settings/export", method = RequestMethod.GET)
    public String exportSettings(HttpServletResponse response) throws IOException, ConfigurationException {
		Map<String, Object> propertiesMap = new HashMap<String, Object>();
    	PropertiesConfiguration properties = new PropertiesConfiguration(MyMediaLifecycle.propertiesFile);
    	Iterator<String> p = properties.getKeys();
    	while (p.hasNext()) {
    		String key = p.next();
    		propertiesMap.put(key, properties.getProperty(key));
    	}
    	
		Map<String, Object> configMap = new HashMap<String, Object>();
		XMLConfiguration config = new XMLConfiguration(MyMediaLifecycle.configFile);
    	Iterator<String> c = config.getKeys();
    	while (c.hasNext()) {
    		String key = c.next();
    		configMap.put(key, config.getProperty(key));
    	}
    	
    	Map<String, Map<String, Object>> settings = new HashMap<String, Map<String, Object>>();
    	settings.put("properties", propertiesMap);
    	settings.put("config", configMap);
    	
    	// export XML
		XStream xstream = new XStream();
		xstream.autodetectAnnotations(true);
		xstream.alias("settings", Map.class);

		byte[] b = xstream.toXML(settings).getBytes();
		response.setHeader("Pragma", "private");
		response.setHeader("Cache-Control", "private, must-revalidate");
		response.setHeader("Content-Disposition","attachment; filename=\"" + IndexController.instanceName + " - settings.xml\"");
		response.setContentType("text/xml");
		response.setContentLength(b.length);
		ServletOutputStream ouputStream = response.getOutputStream();
		ouputStream.write(b);
		ouputStream.flush();
		ouputStream.close(); 		
		
		return null;
    }
    @RequestMapping(value = "/settings/upload", method = RequestMethod.POST)
    public String saveSettings(@ModelAttribute("uploadedFile") UploadedFile uploadedFile) throws IOException, ConfigurationException {
		XStream xstream = new XStream();
		xstream.autodetectAnnotations(true);
		xstream.alias("settings", Map.class);
		Map<String, Map<String, Object>> settings = (HashMap<String, Map<String, Object>>) xstream.fromXML(uploadedFile.getFile().getInputStream());
		
    	boolean restartRequired = false;
    	boolean saveConfig = false;
    	boolean saveProperties = false;
    	
    	PropertiesConfiguration properties = new PropertiesConfiguration(MyMediaLifecycle.propertiesFile);
		XMLConfiguration config = new XMLConfiguration(MyMediaLifecycle.configFile);
		
		Iterator<String> s = settings.keySet().iterator();
    	while (s.hasNext()) {
    		String settingsKey = s.next();
    		if ("properties".equals(settingsKey)) {
    			Map<String, Object> propertiesMap = settings.get(settingsKey);
    			Iterator<String> p = propertiesMap.keySet().iterator();
    	    	while (p.hasNext()) {
    	    		String propertiesKey = p.next();
                	if (!properties.containsKey(propertiesKey) || !propertiesMap.get(propertiesKey).equals(properties.getProperty(propertiesKey))) {
                    	// update swordfishsync.properties
                		properties.setProperty(propertiesKey, propertiesMap.get(propertiesKey)); // need validation
                		System.out.println("[DEBUG] properties - " + propertiesKey + ":" + propertiesMap.get(propertiesKey));
                		saveProperties = true;
                	}
    	    	}
    		}
    		if ("config".equals(settingsKey)) {
    			Map<String, Object> configMap = settings.get(settingsKey);
    			Iterator<String> c = configMap.keySet().iterator();
    	    	while (c.hasNext()) {
    	    		String configKey = c.next();
                	if (!properties.containsKey(configKey) || !configMap.get(configKey).equals(properties.getProperty(configKey))) {
                    	// update config.xml
                		config.setProperty(configKey, configMap.get(configKey)); // need validation
                		System.out.println("[DEBUG] config - " + configKey + ":" + configMap.get(configKey));
                		saveConfig = true;
                	}
    	    	}
    		}
    	}
    	
    	// save changes and reload config
    	if (saveProperties) {
        	restartRequired = true;
        	properties.save();
    	}
    	if (saveConfig) {
    		config.save();
        	MyMediaLifecycle.readConfig();
    	}
    	
		return "redirect:/settings?saved=true&restartRequired=" + Boolean.toString(restartRequired); // needs error message/validation
    }
    
    @RequestMapping(value = "/index/upload", method = RequestMethod.POST)
    public String saveIndex(@ModelAttribute("uploadedFile") UploadedFile uploadedFile, HttpSession session) throws IOException {
    	String filePath = session.getServletContext().getRealPath("/WEB-INF/classes") + System.getProperty("file.separator") + "index.jsp";
    	File file = new File(filePath);
    	if (uploadedFile.getFile() != null && !uploadedFile.getFile().isEmpty()) {
        	uploadedFile.getFile().transferTo(file);
    	}
		return "redirect:/";
    }
    @RequestMapping(value = "/index/download", method = RequestMethod.GET)
    public String downloadIndex(HttpSession session, HttpServletResponse response) throws IOException {
    	File indexFile;
    	
    	Resource customIndex = new ClassPathResource("index.jsp");
    	if (customIndex.exists() && customIndex.isReadable()) {
    		indexFile = customIndex.getFile();
    	} else {
        	String filePath = session.getServletContext().getRealPath("/WEB-INF/jsp") + System.getProperty("file.separator") + "index.jsp";
        	indexFile = new File(filePath);
    	}
    	
    	if (indexFile.exists()) {
    		int length = 0;
    		
    		response.setHeader("Pragma", "private");
    		response.setHeader("Cache-Control", "private, must-revalidate");
    		response.setHeader("Content-Disposition","attachment; filename=\"" + IndexController.instanceName + " - " + indexFile.getName() + "\"");
    		response.setContentType("text/plain");
    		response.setContentLength((int) indexFile.length());
    		
            byte[] byteBuffer = new byte[4096];
            DataInputStream in = new DataInputStream(new FileInputStream(indexFile));
    		ServletOutputStream ouputStream = response.getOutputStream();
            
            // reads the file's bytes and writes them to the response stream
            while ((in != null) && ((length = in.read(byteBuffer)) != -1))
            {
            	ouputStream.write(byteBuffer,0,length);
            }
            
            in.close();
    		ouputStream.flush();
    		ouputStream.close();
    		
        	return null;
    	}
		return "redirect:/settings?fileNotFound=true";
    }
    @RequestMapping(value = "/index/revert", method = RequestMethod.GET)
    public String revertIndex() throws IOException {
    	// delete the custom index file so the default will be rendered
    	boolean deleted = false;
    	Resource customIndex = new ClassPathResource("index.jsp");
    	if (customIndex.exists()) {
    		deleted = customIndex.getFile().delete();
    	}
		return "redirect:/settings?revertedIndex=" + deleted;
    }
    
    private void setFeedValues(FeedProvider feedProvider, ModelAndView mav) {
    	// checkbox fields
		String optionChecked = "checked=\"checked\"";
    	String checkedActive = "";
    	String checkedInitialPopulate = "";
    	String checkedRemoveTorrentOnComplete = "";
    	String checkedExtractRars = "";
    	String checkedDetermineSubDirectory = "";

		if (feedProvider.getFeedInfo().getActive()) {
			checkedActive = optionChecked;
		}
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

		mav.addObject("checkedActive", checkedActive);
		mav.addObject("checkedInitialPopulate", checkedInitialPopulate);
		mav.addObject("checkedRemoveTorrentOnComplete", checkedRemoveTorrentOnComplete);
		mav.addObject("checkedExtractRars", checkedExtractRars);
		mav.addObject("checkedDetermineSubDirectory", checkedDetermineSubDirectory);
		
		// select fields
		Map<String, String> actionOptions = new HashMap<String, String>();
		actionOptions.put("download", "Download");
		actionOptions.put("notify", "Notify");
		mav.addObject("actionOptions", actionOptions);
	}
    
    private ModelAndView setCommonObjects(ModelAndView mav) {
		mav.addObject("title", IndexController.instanceName);
		
		List<String> systemErrors = new ArrayList<String>();
		if (StringUtils.isNotBlank(MyMediaLifecycle.startupError)) {
			systemErrors.add(MyMediaLifecycle.startupError);
		}
		if (StringUtils.isNotBlank(MyMediaLifecycle.torrentHostError)) {
			systemErrors.add(MyMediaLifecycle.torrentHostError);
		}
		mav.addObject("systemErrors", systemErrors);
		
    	return mav;
    }
    
    private boolean saveFeedInfoValues(FeedProvider feedProvider, WebRequest webRequest) {
		String url = webRequest.getParameter("feed_url").trim();
		String name = webRequest.getParameter("feed_name").trim();
		if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(name)) { // need better validation/errors
			
			// need to validate these fields
			/**
			 * 
			##### FEED PROPERTIES ######
			# feed properties
			# isActive - still fetches feed, but does not add torrents
			# syncInterval in minutes
			# action: download/notify/ignore: "download" or "nofify", otherwise no action is taken 
			# uploadLimit in Kbps
			# activityInterval in days
			# deleteInterval in days
			# downloadDirectory only use "/" in directory path
			# filter.action: add/ignore (default ignore)
			# removeTorrentOnComplete: remove torrent from torrent client
			# initialPopulate: add all existing torrents when feed first added
			# determineSubDirectory: moves the file to the sub directory if its determined (TV shows: title/season, Movies: )
			 *
			 *
			 *
### not yet implemented
#feed.1.seedRatioAction = ignore/delete/deleteOnPrerequisites
#feed.1.seedRatio = 1
#feed.1.activityIntervalAction = ignore/delete/deleteOnPrerequisites
#feed.1.activityInterval = 365
#
#mymedia.downloadDirectoryDefault = /data/virtual/Video/
#mymedia.downloadDirectoryTv = /data/virtual/Video/TV/
#mymedia.downloadDirectoryHdMovie = /data/virtual/Video/Movies (HD)/
#mymedia.downloadDirectorySdMovie = /data/virtual/Video/Movies/
#feed.x.downloadDirectory... OVERRIDES
#feed.1.downloadDirectoryDefault = /data/virtual/Video/TV/
			 *
			 *
			 *
			 */
			
			
	    	feedProvider.getFeedInfo().setUrl(url);
	    	feedProvider.getFeedInfo().setName(name);
	    	feedProvider.getFeedInfo().setAction(webRequest.getParameter("feed_action").trim()); // action: "download" or "nofify", otherwise no action is taken
	    	feedProvider.getFeedInfo().setSyncInterval(Integer.parseInt(webRequest.getParameter("feed_syncInterval").trim()));
	    	feedProvider.getFeedInfo().setDeleteInterval(Integer.parseInt(webRequest.getParameter("feed_deleteInterval").trim()));
	    	feedProvider.getFeedInfo().setDownloadDirectory(webRequest.getParameter("feed_downloadDirectory").trim());
	    	feedProvider.getFeedInfo().setUploadLimit(Integer.parseInt(webRequest.getParameter("feed_uploadLimit").trim()));
	    	feedProvider.getFeedInfo().setNotifyEmail(webRequest.getParameter("feed_notifyEmail").trim());
	    	feedProvider.getFeedInfo().setDetailsUrlValueFromRegex(webRequest.getParameter("feed_detailsUrlValueFromRegex").trim());
	    	feedProvider.getFeedInfo().setDetailsUrlFormat(webRequest.getParameter("feed_detailsUrlFormat").trim());
	    	
	    	// other fields
	    	if ("on".equals(webRequest.getParameter("feed_active"))) {
	    		feedProvider.getFeedInfo().setActive(true);
	    	} else {
	    		feedProvider.getFeedInfo().setActive(false);
	    	}
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