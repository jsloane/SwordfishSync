package mymedia.services;  // move this to another package?

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import mymedia.controllers.IndexController;
import mymedia.db.form.FeedInfo;
import mymedia.db.service.FeedInfoService;
import mymedia.db.service.TorrentInfoService;
import mymedia.exceptions.ApplicationException;
import mymedia.services.model.FeedProvider;
import mymedia.services.model.MediaInfo;
import mymedia.services.tasks.SyncTask;
import mymedia.util.EmailManager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.core.io.ClassPathResource;
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
    
    public static String startupError = null;
    
	private final static String defaultPropertiesFile = "default-swordfishsync.properties";
	private final static String userPropertiesFile = System.getProperty("user.home") + System.getProperty("file.separator")
			+ ".swordfishsync" + System.getProperty("file.separator") + "swordfishsync.properties";
	public static String propertiesFile = defaultPropertiesFile;
	public final static String settingsFile = "/settings.xml";
	private volatile boolean isRunning = false;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private long syncInterval = 30; // default 30min
	
	// auth settings
	public static boolean authEnabled = false;
	public static String username = null;
	public static String password = null;
	
	public static boolean acceptAnySslCertificate = true;
	
	public boolean isRunning() {
		return isRunning;
		/*
		 * debug frozen process..?
		 * ps -u tomcat7
		 * kill -QUIT process_id
		 */
		
		
		/**
		 * 
"pool-7-thread-1" prio=10 tid=0x00007f6e6c625000 nid=0xb6c runnable [0x00007f6e7b0c2000]
   java.lang.Thread.State: RUNNABLE
	at java.net.SocketInputStream.socketRead0(Native Method)
	at java.net.SocketInputStream.read(SocketInputStream.java:152)
	at java.net.SocketInputStream.read(SocketInputStream.java:122)
	at sun.security.ssl.InputRecord.readFully(InputRecord.java:442)
	at sun.security.ssl.InputRecord.read(InputRecord.java:480)
	at sun.security.ssl.SSLSocketImpl.readRecord(SSLSocketImpl.java:927)
	- locked <0x00000000f17f8e08> (a java.lang.Object)
	at sun.security.ssl.SSLSocketImpl.performInitialHandshake(SSLSocketImpl.java:1312)
	- locked <0x00000000f17f8db8> (a java.lang.Object)
	at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:1339)
	at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:1323)
	at sun.net.www.protocol.https.HttpsClient.afterConnect(HttpsClient.java:563)
	at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect(AbstractDelegateHttpsURLConnection.java:185)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1300)
	- locked <0x00000000f17f8990> (a sun.net.www.protocol.https.DelegateHttpsURLConnection)
	at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:254)
	- locked <0x00000000f17f8910> (a sun.net.www.protocol.https.HttpsURLConnectionImpl)
	at mymedia.services.model.FeedProvider.getFeedXml(FeedProvider.java:261)
	at mymedia.services.model.FeedProvider.refreshFeed(FeedProvider.java:204)
	at mymedia.services.model.FeedProvider.getTorrents(FeedProvider.java:173)
	at mymedia.services.MediaManager.syncTorrentFeed(MediaManager.java:75)
	at mymedia.services.MediaManager.syncTorrents(MediaManager.java:63)
	at mymedia.services.tasks.SyncTask.run(SyncTask.java:10)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:471)
	at java.util.concurrent.FutureTask.runAndReset(FutureTask.java:304)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$301(ScheduledThreadPoolExecutor.java:178)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:293)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:744)
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
					log.log(Level.WARNING, "Unable to store user properties file, using default settings.", e);
				}
			}
			log.log(Level.INFO, "MyMediaLifecycle.propertiesFile: " + MyMediaLifecycle.propertiesFile);
			
			// read properties
			PropertiesConfiguration config = new PropertiesConfiguration(MyMediaLifecycle.propertiesFile);
			
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
			
			// set authentication details
			if (config.containsKey("mymedia.auth.enabled") && config.containsKey("mymedia.auth.username") && config.containsKey("mymedia.auth.password")) {
				boolean authEnabled = false;
				try {
					authEnabled = config.getBoolean("mymedia.auth.enabled", new Boolean(false));
				} catch (ConversionException e) {
					log.log(Level.WARNING, "Unable to read boolean property mymedia.auth.enabled", e);
				}
				String username = config.getString("mymedia.auth.username");
				String password = config.getString("mymedia.auth.password");
				if (authEnabled && StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
					this.authEnabled = true;
					this.username = username;
					this.password = password;
				}
			}
			
			// set ssl config
			if (config.containsKey("mymedia.auth.enabled") && config.containsKey("mymedia.auth.username") && config.containsKey("mymedia.auth.password")) {
				boolean acceptAnySslCertificate = true;
				try {
					acceptAnySslCertificate = config.getBoolean("mymedia.acceptAnySslCertificate", new Boolean(false));
					this.acceptAnySslCertificate = acceptAnySslCertificate;
				} catch (ConversionException e) {
					log.log(Level.WARNING, "Unable to read boolean property mymedia.acceptAnySslCertificate", e);
				}
			}
			
			try {
				MediaManager.feedProviders = getFeedProviders();
			} catch (Exception e) {
				// set the error message
				Throwable rootCause = ExceptionUtils.getRootCause(e);
				if (rootCause instanceof ConnectException || rootCause instanceof SQLException) {
					startupError = "Cannot connect to database, check settings and try again. Error details: " + ExceptionUtils.getMessage(e) + ". Cause: " + ExceptionUtils.getRootCauseMessage(e);
				} else {
					startupError = ExceptionUtils.getMessage(e) + ". Cause: " + ExceptionUtils.getRootCauseMessage(e);
				}
				throw new ApplicationException("Error loading from database", e);
			}
			
			java.lang.System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
			
			log.log(Level.INFO, "[DEBUG] MyMediaLifecycle.start scheduling sync task: " + syncInterval + " minute intervals");
			scheduler.scheduleAtFixedRate(new SyncTask(), 0, syncInterval, TimeUnit.MINUTES);
		} catch (IOException | ConfigurationException | CannotCreateTransactionException | ApplicationException e) {
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
