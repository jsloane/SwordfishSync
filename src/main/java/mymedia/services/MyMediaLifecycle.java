package mymedia.services;  // move this to another package?

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import mymedia.auth.CustomAuthenticationProvider;
import mymedia.controllers.IndexController;
import mymedia.db.form.FeedInfo;
import mymedia.db.service.FeedInfoService;
import mymedia.db.service.TorrentInfoService;
import mymedia.exceptions.ApplicationException;
import mymedia.services.model.FeedProvider;
import mymedia.services.model.MediaInfo;
import mymedia.services.tasks.SyncTask;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.core.io.ClassPathResource;

import ca.benow.transmission.TransmissionClient;

public class MyMediaLifecycle implements Lifecycle {
	
	private final static Logger log = Logger.getLogger(MyMediaLifecycle.class.getName());
    
    @Autowired
    private FeedInfoService feedInfoService;
    
    @Autowired
    private TorrentInfoService torrentInfoService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    public static String startupError = null;
    public static String torrentHostError = null;
    
	private final static String defaultConfigFile = "default-config.xml";
	private final static String userConfigFile = System.getProperty("user.home") + System.getProperty("file.separator")
			+ ".swordfishsync" + System.getProperty("file.separator") + "config.xml";
	public static String configFile = defaultConfigFile;
	private final static String defaultPropertiesFile = "default-swordfishsync.properties";
	private final static String userPropertiesFile = System.getProperty("user.home") + System.getProperty("file.separator")
			+ ".swordfishsync" + System.getProperty("file.separator") + "swordfishsync.properties";
	public static String propertiesFile = defaultPropertiesFile;
	public final static String settingsFile = "/settings.xml";
	private volatile boolean isRunning = false;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static long syncInterval = 30; // default 30min
	
	public static boolean acceptUntrustedSslCertificate = false;
	
	public static CacheManager cacheManager = CacheManager.getInstance();
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void start() {
		isRunning = true;
		
		System.out.println();
		
		// set hibernate services
		MediaManager.feedInfoService = feedInfoService;
		MediaManager.torrentInfoService = torrentInfoService;
		
		MediaManager.mailManager = (EmailManager) applicationContext.getBean("emailManager"); // should autowire/set in xml?
		
		try {
			
			readConfig();
			
			// determine properties file
			File defaultPropertiesFile = new ClassPathResource(MyMediaLifecycle.defaultPropertiesFile).getFile();
			File userPropertiesFile = new File(MyMediaLifecycle.userPropertiesFile);
			if (userPropertiesFile.exists()) {
				// check if userPropertiesFile is missing properties from propertiesFile and add any
				PropertiesConfiguration defaultProperties = new PropertiesConfiguration(MyMediaLifecycle.defaultPropertiesFile);
				PropertiesConfiguration userProperties = new PropertiesConfiguration(MyMediaLifecycle.userPropertiesFile);
				Iterator<String> defaultPropertiesKeys = defaultProperties.getKeys();
			    while (defaultPropertiesKeys.hasNext()){
			        String defaultPropertiesKey = defaultPropertiesKeys.next();
			        if (!userProperties.containsKey(defaultPropertiesKey)) {
			        	userProperties.addProperty(defaultPropertiesKey, defaultProperties.getProperty(defaultPropertiesKey));
			        }
			    }
			    userProperties.save();
				
				MyMediaLifecycle.propertiesFile = MyMediaLifecycle.userPropertiesFile;
			} else {
				// copy default properties file to users directory
				try {
					FileUtils.copyFile(defaultPropertiesFile, userPropertiesFile);
					MyMediaLifecycle.propertiesFile = MyMediaLifecycle.userPropertiesFile;
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to store user properties file, using default properties.", e);
				}
			}
			log.log(Level.INFO, "MyMediaLifecycle.propertiesFile: " + MyMediaLifecycle.propertiesFile);
			
			try {
				MediaManager.feedProviders = getFeedProviders();
			} catch (Exception e) {
				e.printStackTrace();
				// set the error message
				Throwable rootCause = ExceptionUtils.getRootCause(e);
				if (rootCause instanceof ConnectException || rootCause instanceof SQLException) {
					startupError = "[" + new Date() + "] Cannot connect to database, check settings and try again. Error details: " + ExceptionUtils.getMessage(e) + ". Cause: " + ExceptionUtils.getRootCauseMessage(e);
				} else {
					startupError = ExceptionUtils.getMessage(e) + ". Cause: " + ExceptionUtils.getRootCauseMessage(e);
				}
				throw new ApplicationException("Error loading from database", e);
			}
			
			Cache memoryOnlyCache = new Cache("torrentClientData", 1, false, false, 15, 15);
			cacheManager.addCache(memoryOnlyCache);
			
			log.log(Level.INFO, "[DEBUG] MyMediaLifecycle.start scheduling sync task: " + syncInterval + " minute intervals");
			scheduler.scheduleAtFixedRate(new SyncTask(), 0, syncInterval, TimeUnit.MINUTES);
		} catch (IOException | ConfigurationException | ApplicationException e) {
			// need an error handler to email severe errors? (not rss feed connection errors)
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.log(Level.SEVERE, "[DEBUG] STARTUP FAILED, JOB NOT SCHEDULED ################");
		}
	}
	
	public static void readConfig() throws IOException, ConfigurationException {
		
		String hostName = java.net.InetAddress.getLocalHost().getHostName();
		
		// determine config file
		File defaultConfigFile = new ClassPathResource(MyMediaLifecycle.defaultConfigFile).getFile();
		File userConfigFile = new File(MyMediaLifecycle.userConfigFile);
		if (userConfigFile.exists()) {
			// check if userConfigFile is missing config from defaultConfigFile and add any
			XMLConfiguration defaultConfig = new XMLConfiguration(MyMediaLifecycle.defaultConfigFile);
			XMLConfiguration userConfig = new XMLConfiguration(MyMediaLifecycle.userConfigFile);
			Iterator<String> defaultConfigKeys = defaultConfig.getKeys();
		    while (defaultConfigKeys.hasNext()) {
		        String defaultConfigKey = defaultConfigKeys.next();
		        if (!userConfig.containsKey(defaultConfigKey)) {
		        	// add missing property to from default-config to user-config
		        	userConfig.addProperty(defaultConfigKey, defaultConfig.getProperty(defaultConfigKey));
		        } else if (defaultConfigKey.contains("[@") && userConfig.getProperty(defaultConfigKey) != defaultConfig.getProperty(defaultConfigKey)) {
		        	// update attribute value
		        	userConfig.setProperty(defaultConfigKey, defaultConfig.getProperty(defaultConfigKey));
		        }
		    }
			Iterator<String> userConfigKeys = userConfig.getKeys();
		    while (userConfigKeys.hasNext()) {
		        String userConfigKey = userConfigKeys.next();
		    	if (!defaultConfig.containsKey(userConfigKey) && userConfigKey.contains("[@")) {
		    		userConfig.clearProperty(userConfigKey);
		    	}
		    }
		    userConfig.save();
			
			MyMediaLifecycle.configFile = MyMediaLifecycle.userConfigFile;
		} else {
			// copy default properties file to users directory
			try {
				FileUtils.copyFile(defaultConfigFile, userConfigFile);
				MyMediaLifecycle.configFile = MyMediaLifecycle.userConfigFile;
			} catch (IOException e) {
				log.log(Level.WARNING, "Unable to store user config file, using default config.", e);
			}
		}
		log.log(Level.INFO, "MyMediaLifecycle.configFile: " + MyMediaLifecycle.configFile);
		
		XMLConfiguration config = new XMLConfiguration(MyMediaLifecycle.configFile);
		
		if (config.containsKey("application.debugonhost")) {
	    	for (String debugHost : config.getStringArray("application.debugonhost")) {
				if (hostName.equalsIgnoreCase(debugHost)) {
					MediaManager.debug = true;
				}
	    	}
		}
		if (config.containsKey("application.title")) {
			IndexController.instanceName = config.getString("application.title");
		}
		
		log.log(Level.INFO, "[DEBUG] MyMediaLifecycle.readConfig: " + IndexController.instanceName + ", running on " + hostName);
		
		syncInterval = Long.parseLong(config.getString("application.syncinterval", "0"));
		
		String transmissionHost = config.getString("torrentclient.host");
		int transmissionPort = Integer.parseInt(config.getString("torrentclient.port"));
		String transmissionUser = config.getString("torrentclient.username");
		String transmissionPass = config.getString("torrentclient.password");
		MediaManager.torrentClient = new TransmissionClient(transmissionHost, transmissionPort, transmissionUser, transmissionPass);
		

		if (config.containsKey("media.tvdb.apikey")) {
			MediaInfo.tvdbApiKey = config.getString("media.tvdb.apikey");
			if (config.containsKey("media.tvdb.notice")) {
				MediaInfo.tvdbApiNotice = config.getString("media.tvdb.notice");
			}
		}
		if (config.containsKey("media.tmdb.apikey")) {
			MediaInfo.tmdbApiKey = config.getString("media.tmdb.apikey");
			if (config.containsKey("media.tmdb.notice")) {
				MediaInfo.tmdbApiNotice = config.getString("media.tmdb.notice");
			}
		}
		
		// set authentication details
		if (config.containsKey("application.security.basicauth.enabled") && config.containsKey("application.security.basicauth.username") && config.containsKey("application.security.basicauth.password")) {
			boolean authEnabled = false;
			try {
				authEnabled = config.getBoolean("application.security.basicauth.enabled", new Boolean(false));
			} catch (ConversionException e) {
				log.log(Level.WARNING, "Unable to read boolean property application.security.basicauth.enabled", e);
			}
			String username = config.getString("application.security.basicauth.username");
			String password = config.getString("application.security.basicauth.password");
			if (authEnabled && StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
				CustomAuthenticationProvider.authEnabled = true;
				CustomAuthenticationProvider.username = username;
				CustomAuthenticationProvider.password = password;
			}
		}
		
		// set ssl config
		if (config.containsKey("application.security.acceptAnySslCertificate")) {
			try {
				acceptUntrustedSslCertificate = config.getBoolean("application.security.acceptanysslcertificate", new Boolean(false));
			} catch (ConversionException e) {
				log.log(Level.WARNING, "Unable to read boolean property application.security.acceptanysslcertificate", e);
			}
		}
	}

	public void stop() {
		scheduler.shutdown();
		isRunning = false;
	}
	
	private List<FeedProvider> getFeedProviders() {
		// read in and init feeds
    	// properties file is a baseline that overrides database records
		List<FeedProvider> feedProviders = new ArrayList<FeedProvider>();
    	List<Integer> gotIds = new ArrayList<Integer>();
    	
    	// get other feeds from db that are not in properties file
    	Integer[] gotIdsArray = (Integer[]) gotIds.toArray(new Integer[gotIds.size()]);
    	List<FeedInfo> feedInfos = MediaManager.feedInfoService.getMissingFeedInfos(gotIdsArray);
    	for (FeedInfo feedInfo : feedInfos) {
    		feedProviders.add(new FeedProvider(feedInfo));
    	}
    	
		return feedProviders;
	}
}
