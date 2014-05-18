package mymedia.services;  // move this to another package?

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import mymedia.controllers.IndexController;
import mymedia.db.form.FeedInfo;
import mymedia.db.form.FilterAttribute;
import mymedia.db.service.FeedInfoService;
import mymedia.db.service.TorrentInfoService;
import mymedia.model.xsd.Settings;
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
	public final static String settingsFile = "/settings.xml";
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
			scheduler.scheduleAtFixedRate(new SyncTask(), 0, syncInterval, TimeUnit.MINUTES);
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
    	
    	// get other feeds from db that are not in properties file
    	Integer[] gotIdsArray = (Integer[]) gotIds.toArray(new Integer[gotIds.size()]);
    	List<FeedInfo> feedInfos = MediaManager.feedInfoService.getMissingFeedInfos(gotIdsArray);
    	for (FeedInfo feedInfo : feedInfos) {
    		feedProviders.add(new FeedProvider(feedInfo));
    	}
    	
		return feedProviders;
	}
}
