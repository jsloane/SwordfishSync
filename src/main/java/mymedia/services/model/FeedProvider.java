package mymedia.services.model;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jdom.Element;

import mymedia.db.form.FeedInfo;
import mymedia.db.form.FilterAttribute;
import mymedia.db.form.TorrentInfo;
import mymedia.services.MediaManager;
import mymedia.services.MyMediaLifecycle;

import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedProvider {

	private final static Logger log = Logger.getLogger(MyMediaLifecycle.class.getName());
	
	private int ttl = 0; // ttl is in minutes (from rss spec?)
	private long lastFetched = 0; // when the feed was last fetched (in minutes)
	private Date lastUpdated;
	private FeedInfo feedInfo;
	private boolean isFeedCurrent = false; // use this to check if an exception occurred (connection timeout, etc) when checking completed torrents to remove
	private String statusMessage = "";
	private List<TorrentInfo> torrentsFromFeed;

	public FeedProvider() {
		this.feedInfo = new FeedInfo();
	}
	public FeedProvider(String url) {
		initDb(url);
	}
	public FeedProvider(FeedInfo feedInfo) {
		this.feedInfo = feedInfo;
	}

	public boolean isFeedCurrent() {
		return isFeedCurrent;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public int getTtl() {
		return ttl;
	}
	public long getLastFetched() {
		return lastFetched;
	}
	public Date getDateFetched() {
		if (lastFetched != 0) {
			return new Date(lastFetched);
		}
		return null;
	}
	public Date getDateUpdated() {
		return lastUpdated;
	}
	
	public void saveFeedInfo() {
		MediaManager.feedInfoService.saveFeedInfo(feedInfo);
	}
	
	// save to db
	public void saveTorrent(TorrentInfo torrentInfo) {
		TorrentInfo existingTorrentInfo = null;
		for (TorrentInfo torrentRecord : feedInfo.getFeedTorrents()) {
			if (torrentRecord != null && torrentRecord.getUrl() != null && torrentRecord.getUrl().equalsIgnoreCase(torrentInfo.getUrl())) { // should override and use equals method
				existingTorrentInfo = torrentRecord;
			}
		}
		if (existingTorrentInfo != null) {
			feedInfo.getFeedTorrents().remove(existingTorrentInfo);
		}

		feedInfo.getFeedTorrents().add(torrentInfo);
		MediaManager.feedInfoService.saveFeedInfo(feedInfo);
		//MediaManager.torrentInfoService.saveTorrentInfo(torrentInfo);
	}

	public void saveNewTorrent(TorrentInfo torrentInfo) {
		// check if we already have this torrent, by name or url
		boolean existingTorrent = false;
		for (TorrentInfo torrentRecord : feedInfo.getFeedTorrents()) {
			try {
				if (torrentRecord != null && torrentRecord.getUrl() != null && torrentRecord.getName() != null &&
						torrentInfo != null && torrentInfo.getUrl() != null && torrentInfo.getName() != null && (
						torrentRecord.getUrl().trim().equalsIgnoreCase(torrentInfo.getUrl().trim())
						|| torrentRecord.getName().trim().equalsIgnoreCase(torrentInfo.getName().trim())
					)
				) {
					existingTorrent = true;
				} else if (torrentRecord != null && torrentRecord.getUrl() != null &&
						torrentInfo != null && torrentInfo.getUrl() != null &&
						torrentRecord.getUrl().trim().equalsIgnoreCase(torrentInfo.getUrl().trim())) {
					existingTorrent = true;
				}
			} catch (Exception ex) {
				System.out.println("TEST - recordTorrentFromFeed");
				ex.printStackTrace();
			}
		}
		if (!existingTorrent) {
			// if new torrent, save to db
			feedInfo.getFeedTorrents().add(torrentInfo);
			MediaManager.feedInfoService.saveFeedInfo(feedInfo);
			//MediaManager.torrentInfoService.saveTorrentInfo(torrentInfo);
		}
	}
	public void removeTorrent(TorrentInfo torrent) {
		feedInfo.getFeedTorrents().remove(torrent);
		log.log(Level.INFO, "[DEBUG] FeedProvider.removeTorrent() torrent: " + torrent);
		//Log.log(Level.INFO, "[DEBUG] FeedProvider.removeTorrent() MediaManager.feedInfoService.saveFeedInfo");
		MediaManager.feedInfoService.saveFeedInfo(feedInfo);
		//Log.log(Level.INFO, "[DEBUG] FeedProvider.removeTorrent() MediaManager.torrentInfoService.removeTorrentInfo(torrent)");
		MediaManager.torrentInfoService.removeTorrentInfo(torrent);
		//Log.log(Level.INFO, "[DEBUG] FeedProvider.removeTorrent() done.");
	}
	
	public void removeFeedInfo() {
		MediaManager.feedInfoService.removeFeedInfo(feedInfo);
		MediaManager.feedProviders.remove(this);
	}
	
	private void initDb(String url) {
		FeedInfo feedInfo = new FeedInfo();
		feedInfo.setUrl(url);
		this.feedInfo = MediaManager.feedInfoService.initFeedInfo(feedInfo);
	}
	
	
	public List<TorrentInfo> getTorrents() { // this includes all torrents in the DB, and fetched torrents from the rss feed
		log.log(Level.INFO, "FeedProvider.getTorrents() Fetching feed: " + feedInfo.getName());
		
		boolean refreshFeed = false;
		int syncInterval = feedInfo.getSyncInterval();
		if (syncInterval != 0 || ttl != 0) {
			double fetchedInterval = Math.ceil((double)(new Date().getTime() - lastFetched) / 1000 / 60); // round up to the nearest minute
			if (syncInterval != 0) {
				if (fetchedInterval >= syncInterval) {
					// if sync interval has passed, refresh feed
					refreshFeed = true;
				}
			} else if (ttl != 0 && fetchedInterval >= ttl) {
				// if ttl has passed and sync interval is not specified, refresh feed
				refreshFeed = true;
			}
		} else {
			// refresh feed anyway
			refreshFeed = true;
		}

		//LOG.log(Level.INFO, "[DEBUG] getTorrents refreshFeed: "+refreshFeed);
		if (refreshFeed) {
			isFeedCurrent = false;
			statusMessage = "";
			try {
				refreshFeed();
				if (!feedInfo.getInitilised()) {
					feedInfo.setInitilised(true);
					saveFeedInfo();
				}
			} catch (Exception e) {
				System.out.println("TEST - getTorrents");
				e.printStackTrace();
				if (statusMessage.isEmpty()) {
					statusMessage = e.toString();
				}
			}
			log.log(Level.INFO, "FeedProvider.getTorrents() Fetched feed: " + feedInfo.getName() + ", "
					+ "current: " + isFeedCurrent
					+ ", ttl: " + ttl);
		}
		
		return feedInfo.getFeedTorrents();
	}

	public FeedInfo getFeedInfo() {
		return this.feedInfo;
	}
	public List<TorrentInfo> getTorrentsFromFeed() { // return only whats in the xml feed
		return this.torrentsFromFeed;
	}
    
	private void refreshFeed() throws Exception {
		lastFetched = new Date().getTime();
		torrentsFromFeed = new ArrayList<TorrentInfo>();
		
		// https://rometools.jira.com/wiki/display/ROME/Preserving+WireFeeds
		SyndFeed feed = getFeedXml();
        if (feed != null) {
			String type = feed.getFeedType();
			// if RSS feed, check for TTL
	        if (type.contains("rss")) {
				Channel channel = (Channel) feed.originalWireFeed();
				ttl = channel.getTtl();
	        }
	        
	        for (Iterator<?> i = feed.getEntries().iterator(); i.hasNext();) {
	        	SyndEntry entry = (SyndEntry) i.next();
	        	
	        	Map<String, String> expandedData = new HashMap<String, String>();
	        	
	        	// check for extra data
	        	for (Element element : (List<Element>) entry.getForeignMarkup()) { 
	            	expandedData.put(
	        			element.getName(),
						element.getValue()
	    			);
	        	}
	        	
	        	Date dateAdded = entry.getPublishedDate();
	        	if (dateAdded == null) {
	        		dateAdded = new Date();
	        	}
	        	
	        	String torrentStatus = TorrentInfo.STATUS_NOT_ADDED;
	        	if (!feedInfo.getInitilised() && !feedInfo.getInitialPopulate()) {
	        		torrentStatus = TorrentInfo.STATUS_SKIPPED; // skip existing feed entries when adding feed
	        	}
	        	
	        	TorrentInfo newTorrent = new TorrentInfo(
					entry.getTitle(),
					entry.getLink(),
					dateAdded,
					expandedData,
					torrentStatus
	    		);
	        	
	        	saveNewTorrent(newTorrent);
	        	torrentsFromFeed.add(newTorrent);
				isFeedCurrent = true;
				lastUpdated = new Date();
	        }
        }
	}
	
	private SyndFeed getFeedXml() {
        XmlReader reader = null;
        SyndFeed feed = null;
        HttpGet httpGet = null;
        CloseableHttpResponse httpResponse = null;
        try {
        	CloseableHttpClient httpClient = null;
        	if (MyMediaLifecycle.acceptUntrustedSslCertificate) {
	        	// accept any SSL certificate
	        	SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(
					null, new TrustStrategy() {
						@Override
						public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
							return true;
						}
					}
				).useTLS().build();
	        	httpClient = HttpClients.custom()
	        			.setSslcontext(sslContext)
	        			.build();
        	} else {
        		httpClient = HttpClients.custom().build();
        	}
        	
    		// set http configuration
        	RequestConfig requestConfig = RequestConfig.custom()
        		    .setSocketTimeout(120000)
        		    .setConnectTimeout(120000)
        		    .setConnectionRequestTimeout(120000)
        		    //.setLocalAddress(localAddress)
        		    .build();
    		httpGet = new HttpGet(feedInfo.getUrl());
    		httpGet.setConfig(requestConfig);
    		httpResponse = httpClient.execute(httpGet);
    		
    		reader = new XmlReader(httpResponse.getEntity().getContent());
    		
			SyndFeedInput in = new SyndFeedInput();
			in.setPreserveWireFeed(true);
            feed = in.build(reader);
    		log.log(Level.INFO, "[DEBUG] FeedProvider.getFeedXml() DONE.");
        } catch (Exception ex) {
			System.out.println("TEST - getFeedXml 1");
        	ex.printStackTrace();
			statusMessage = ex.toString();
        } finally {
	        if (reader != null) {
	        	try {
	        		reader.close();
				} catch (Exception e) {
					System.out.println("TEST - getFeedXml 2");
					e.printStackTrace();
				}
	        }
	        if (httpResponse != null) {
	        	try {
					httpResponse.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        if (httpGet != null) {
	        	httpGet.releaseConnection();
	        }
	    }
		return feed;
	}
	
	public List<TorrentInfo> getTorrentsInprogress() {
		return getTorrentsForStatus(new String[] {TorrentInfo.STATUS_IN_PROGRESS, TorrentInfo.STATUS_NOTIFY_COMPLETED});
	}
	
	public List<TorrentInfo> getTorrentsForStatus(String[] status) {
		List<TorrentInfo> torrentsForStatus = new ArrayList<TorrentInfo>();
		
		for (TorrentInfo torrentInfo : feedInfo.getFeedTorrents()) {
			for (String torrentStatus : status) {
				if (torrentStatus.equals(torrentInfo.getStatus())) {
					torrentsForStatus.add(torrentInfo);
				}
			}
		}
		
		return torrentsForStatus;
	}
    
    public String getTorrentDetailsUrl(TorrentInfo torrentInfo) {
    	if (torrentInfo == null) {
    		return null;
    	}
		String url = torrentInfo.getUrl();

		// check if feed has custom notification link url defined
		if (!StringUtils.isBlank(getFeedInfo().getDetailsUrlValueFromRegex()) && !StringUtils.isBlank(getFeedInfo().getDetailsUrlFormat())) {
			Pattern pUrl = Pattern.compile(getFeedInfo().getDetailsUrlValueFromRegex());
			if (pUrl != null && url != null) {
				Matcher mUrl = pUrl.matcher(url);
				if (mUrl.matches()) {
					url = getFeedInfo().getDetailsUrlFormat().replace("{regex-value}", mUrl.group(1));
				}
			}
		}
		
		return url;
    }
	
	public boolean checkFilterMatch(String value) {
		/*if (!enableFilter) {
			return true;
		}*/
		boolean defaultAction = false;
		
		if (feedInfo.getFilterAction().equalsIgnoreCase("add")) {
			defaultAction = true;
		}
		
		// property: precedence = ignore
		/*
		    filter
			action when no match: add/ignore (ignore) - defaultAction
			filter precedence add/ignore (ignore) - matchFirst
		 */
		
		String matchFirst = feedInfo.getFilterPrecedence();
		String matchSecond;
		if (matchFirst == null || matchFirst.isEmpty()) {
			matchFirst = "ignore";
		}
		if (matchFirst.equals("ignore")) {
			matchSecond = "add";
		} else if (matchFirst.equals("add")) {
			matchSecond = "ignore";
		} else {
			matchFirst = "ignore";
			matchSecond = "add";
		}
		
		if (checkFilter(matchFirst, value)) {
			if (matchFirst.equals("add")) {
				return true;
			} else if (matchFirst.equals("ignore")) {
				return false;
			}
		}
		if (checkFilter(matchSecond, value)) {
			if (matchSecond.equals("add")) {
				return true;
			} else if (matchSecond.equals("ignore")) {
				return false;
			}
		}
		
		return defaultAction;
	}
	
    private boolean checkFilter(String filterType, String value) {
		boolean match = false;
		boolean removeMatchedRegex = false;
		
		// only remove entries from add watchlist
		if (filterType.equals("add")) {
			removeMatchedRegex = feedInfo.getRemoveAddFilterOnMatch();
		}
		
		List<FilterAttribute> removeList = new ArrayList<FilterAttribute>();
        for (FilterAttribute filterAttribute : feedInfo.getFilterAttributes()) {
    		if (filterAttribute.getFilterType().equalsIgnoreCase(filterType) && value != null && value.matches(filterAttribute.getFilterRegex())) {
    			match = true;
    			if (removeMatchedRegex) {
    				// remove matched regex from filter
    				// or have it as a property of the regex string/entry, when the ui is done... THIS? (only for the add/ignore filter)
    				removeList.add(filterAttribute);
    			}
    			break;
    		}
    	}
        
        if (!removeList.isEmpty()) {
        	// update watchlist
            for (FilterAttribute filterAttribute : removeList) {
            	feedInfo.getFilterAttributes().remove(filterAttribute);
        	}
        	saveFeedInfo();
        }
    	
		return match;
	}
    
    public boolean shouldAddTorrent(TorrentInfo torrentInfo) {
    	return !this.getFeedInfo().getFilterEnabled() || this.checkFilterMatch(torrentInfo.getName());
    }
	
	public String toString() {
        return "FeedProvider: ttl [" + ttl + "], lastFetched [" + lastFetched + "], feedInfo [" + feedInfo + "]";
        // upload limit, finish dir, etc
    }
}
