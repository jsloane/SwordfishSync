package mymedia.services;  // move this to another package?

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import mymedia.controllers.IndexController;
import mymedia.db.form.FeedInfo;
import mymedia.db.form.FilterAttribute;
import mymedia.db.service.FeedInfoService;
import mymedia.db.service.TorrentInfoService;
import mymedia.services.model.FeedProvider;
import mymedia.services.model.MediaInfo;
import mymedia.services.tasks.SyncTask;
import mymedia.util.EmailManager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.transaction.CannotCreateTransactionException;

import ca.benow.transmission.TransmissionClient;

public class MyMediaLifecycle implements Lifecycle {
	
	private final static Logger log = Logger.getLogger(MyMediaLifecycle.class.getName());
    
    @Autowired
    private FeedInfoService feedInfoService;
    
    @Autowired
    private TorrentInfoService torrentInfoService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
	public final static String propertiesFile = "/mymedia.properties";
	private volatile boolean isRunning = false;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private long syncInterval = 30; // default 30min
	
	public boolean isRunning() {
		return isRunning;
		/*
		 * debug frozen process..?
		 * ps -u tomcat7
		 * kill -QUIT process_id
		 */
	}

	public void start() {
		isRunning = true;
		
		// set hibernate services
		MediaManager.feedInfoService = feedInfoService;
		MediaManager.torrentInfoService = torrentInfoService;
		
		MediaManager.mailManager = (EmailManager) applicationContext.getBean("emailManager"); // should autowire/set in xml?
		
		try {
			String hostName = java.net.InetAddress.getLocalHost().getHostName();
			
			PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);
			
			if (config.containsKey("mymedia.debugOnHost")) {
		    	for (String debugHost : config.getStringArray("mymedia.debugOnHost")) {
					if (hostName.equalsIgnoreCase(debugHost)) {
						MediaManager.debug = true;
					}
		    	}
			}
			if (config.containsKey("mymedia.title")) {
				IndexController.instanceName = config.getString("mymedia.title");
			}
	    	
			log.log(Level.INFO, "[DEBUG] MyMediaLifecycle.start: " + config.getString("mymedia.title") + ", running on " + hostName);
			
			syncInterval = Long.parseLong(config.getString("mymedia.syncInterval"));
			
			String transmissionHost = config.getString("torrentclient.host");
			int transmissionPort = Integer.parseInt(config.getString("torrentclient.port"));
			String transmissionUser = config.getString("torrentclient.username");
			String transmissionPass = config.getString("torrentclient.password");
			MediaManager.torrentClient = new TransmissionClient(transmissionHost, transmissionPort, transmissionUser, transmissionPass);
	    	
			if (config.containsKey("mymedia.tvdbApiKey")) {
				MediaInfo.tvdbApiKey = config.getString("mymedia.tvdbApiKey");
				if (config.containsKey("mymedia.tmdbApiKey")) {
					MediaInfo.tvdbApiNotice = config.getString("mymedia.tvdbApiNotice");
				}
			}
			if (config.containsKey("mymedia.tmdbApiKey")) {
				MediaInfo.tmdbApiKey = config.getString("mymedia.tmdbApiKey");
				if (config.containsKey("mymedia.tmdbApiKey")) {
					MediaInfo.tmdbApiNotice = config.getString("mymedia.tmdbApiNotice");
				}
			}
			
			MediaManager.feedProviders = getFeedProviders(config);
			
			log.log(Level.INFO, "[DEBUG] MyMediaLifecycle.start scheduling sync task: " + syncInterval + " minute intervals");
			scheduler.scheduleAtFixedRate(new SyncTask(), 0/*1*/, syncInterval, TimeUnit.MINUTES); // one minute before process starts
		} catch (IOException | ConfigurationException | CannotCreateTransactionException e) {
			// need an error handler to email severe errors? (not rss feed connection errors)
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.log(Level.SEVERE, "[DEBUG] STARTUP FAILED, JOB NOT SCHEDULED ################");
		}
	}
	
	public void stop() {
		scheduler.shutdown();
		isRunning = false;
	}
	
	private List<FeedProvider> getFeedProviders(PropertiesConfiguration config) {
		// read in and init feeds
    	// properties file is a baseline that overrides database records
		List<FeedProvider> feedProviders = new ArrayList<FeedProvider>();
    	List<Integer> gotIds = new ArrayList<Integer>();
    	if (config.containsKey("feed.count")) {
    		int feedCount = Integer.parseInt(config.getString("feed.count"));
	    	if (feedCount > 0) {
	            for (int i = 1; i <= feedCount; i++) {
	        		if (config.containsKey("feed." + i + ".url")) {
		    			log.log(Level.INFO, "[DEBUG] MyMediaLifecycle.start reading in feed: " + i);
		    			// this may overwrite other feed settings, if there are other feeds in the DB with this URL. Can't prevent this because we don't know feed ID.
		            	FeedProvider readFeedProvider = new FeedProvider(config.getString("feed." + i + ".url"));
		            	readFeedProvider.setFromPropertiesFile(true);
	        			if (config.containsKey("feed." + i + ".isActive")) {
	        				readFeedProvider.getFeedInfo().setActive(Boolean.parseBoolean(config.getString("feed." + i + ".isActive")));
	        			}
	        			
	        			if (config.containsKey("feed." + i + ".initialPopulate")) {
	        				readFeedProvider.getFeedInfo().setInitialPopulate(config.getBoolean("feed." + i + ".initialPopulate"));
	        			}
	        			if (config.containsKey("feed." + i + ".action")) {
	        				readFeedProvider.getFeedInfo().setAction(config.getString("feed." + i + ".action"));
	        			}
	        			if (config.containsKey("feed." + i + ".name")) {
	        				readFeedProvider.getFeedInfo().setName(config.getString("feed." + i + ".name"));
	        			}
	        			if (config.containsKey("feed." + i + ".syncInterval")) {
	        				readFeedProvider.getFeedInfo().setSyncInterval(Integer.parseInt(config.getString("feed." + i + ".syncInterval")));
	        			}
	        			if (config.containsKey("feed." + i + ".downloadDirectory")) {
	        				readFeedProvider.getFeedInfo().setDownloadDirectory(config.getString("feed." + i + ".downloadDirectory"));
	        			}
	        			if (config.containsKey("feed." + i + ".uploadLimit")) {
	        				readFeedProvider.getFeedInfo().setUploadLimit(Integer.parseInt(config.getString("feed." + i + ".uploadLimit")));
	        			}
	        			if (config.containsKey("feed." + i + ".deleteInterval")) {
	        				readFeedProvider.getFeedInfo().setDeleteInterval(Integer.parseInt(config.getString("feed." + i + ".deleteInterval")));
	        			}
	        			if (config.containsKey("feed." + i + ".notifyEmail")) {
	        				readFeedProvider.getFeedInfo().setNotifyEmail(config.getString("feed." + i + ".notifyEmail"));
	        			}
	        			if (config.containsKey("feed." + i + ".extractRars")) {
	        				readFeedProvider.getFeedInfo().setExtractRars(config.getBoolean("feed." + i + ".extractRars"));
	        			}
	        			if (config.containsKey("feed." + i + ".determineSubDirectory")) {
	        				readFeedProvider.getFeedInfo().setDetermineSubDirectory(config.getBoolean("feed." + i + ".determineSubDirectory"));
	        			}
	        			if (config.containsKey("feed." + i + ".filter")) {
	        				readFeedProvider.getFeedInfo().setFilterEnabled(Boolean.parseBoolean(config.getString("feed." + i + ".filter")));
	        			}
	        			if (config.containsKey("feed." + i + ".filter.action")) {
	        				readFeedProvider.getFeedInfo().setFilterAction(config.getString("feed." + i + ".filter.action"));
	        			}
	        			if (config.containsKey("feed." + i + ".filter.add")) {
	            			Set<FilterAttribute> filterAttributes = readFeedProvider.getFeedInfo().getFilterAttributes();
	        		    	for (String regex : config.getStringArray("feed." + i + ".filter.add")) {
	                			filterAttributes.add(
	                				new FilterAttribute("add", regex.trim())
	                			);
	        		    	}
	            			readFeedProvider.getFeedInfo().setFilterAttributes(filterAttributes);
	        			}
	        			if (config.containsKey("feed." + i + ".filter.ignore")) {
	            			Set<FilterAttribute> filterAttributes = readFeedProvider.getFeedInfo().getFilterAttributes();
	        		    	for (String regex : config.getStringArray("feed." + i + ".filter.ignore")) {
	                			filterAttributes.add(
	                				new FilterAttribute("ignore", regex.trim())
	                			);
	        		    	}
	            			readFeedProvider.getFeedInfo().setFilterAttributes(filterAttributes);
	        			}
	        			if (config.containsKey("feed." + i + ".filter.precedence")) {
	        				readFeedProvider.getFeedInfo().setFilterPrecedence(config.getString("feed." + i + ".filter.precedence"));
	        			}
	        			if (config.containsKey("feed." + i + ".filter.removeAddFilterOnMatch")) {
	        				readFeedProvider.getFeedInfo().setRemoveAddFilterOnMatch(config.getBoolean("feed." + i + ".filter.removeAddFilterOnMatch"));
	        			}
	        			if (config.containsKey("feed." + i + ".removeTorrentOnComplete")) {
	        				readFeedProvider.getFeedInfo().setRemoveTorrentOnComplete(config.getBoolean("feed." + i + ".removeTorrentOnComplete"));
	        			}
	        			// activityDateInterval
	        			// seedRatio
	        			// etc
	        			
	        			readFeedProvider.saveFeedInfo();
	        			
	        			feedProviders.add(readFeedProvider);
	        			gotIds.add(readFeedProvider.getFeedInfo().getId());
	        		}
	            }
	    	}
    	}
    	
    	// get other feeds from db that are not in properties file
    	Integer[] gotIdsArray = (Integer[]) gotIds.toArray(new Integer[gotIds.size()]);
    	List<FeedInfo> feedInfos = MediaManager.feedInfoService.getMissingFeedInfos(gotIdsArray);
    	for (FeedInfo feedInfo : feedInfos) {
    		feedProviders.add(new FeedProvider(feedInfo));
    	}
    	
		return feedProviders;
	}
}
