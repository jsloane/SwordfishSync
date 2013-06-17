package mymedia.services.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private final static Logger Log = Logger.getLogger(MyMediaLifecycle.class.getName());
	
	private int ttl = 0; // ttl is in minutes (from rss spec?)
	private long lastFetched = 0; // when the feed was last fetched (in minutes?)
	private FeedInfo feedInfo;
	private boolean isFeedCurrent = false; // use this to check if an exception occurred (connection timeout, etc) when checking completed torrents to remove
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
	public int getTtl() {
		return ttl;
	}
	public long getLastFetched() {
		return lastFetched;
	}
	
	public void saveFeedInfo() {
		MediaManager.feedInfoService.saveFeedInfo(feedInfo);
	}
	
	// save to db
	public void saveTorrent(TorrentInfo torrentInfo) {
		TorrentInfo existingTorrentInfo = null;
		for (TorrentInfo torrentRecord : feedInfo.getFeedTorrents()) {
			if (torrentRecord.getUrl().equalsIgnoreCase(torrentInfo.getUrl())) {
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

	private void recordTorrentFromFeed(TorrentInfo torrentInfo) {
		// check if we already have this torrent, by name or url
		boolean existingTorrent = false;
		for (TorrentInfo torrentRecord : feedInfo.getFeedTorrents()) {
			try {
				if (torrentRecord != null && (
						torrentRecord.getUrl().trim().equalsIgnoreCase(torrentInfo.getUrl().trim()) ||
						torrentRecord.getName().trim().equalsIgnoreCase(torrentInfo.getName().trim())
					)
				) {
					existingTorrent = true;
				}
			} catch (Exception ex) {
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
		Log.log(Level.INFO, "[DEBUG] FeedProvider.removeTorrent() torrent: " + torrent);
		MediaManager.feedInfoService.saveFeedInfo(feedInfo);
		MediaManager.torrentInfoService.removeTorrentInfo(torrent);
	}
	
	private void initDb(String url) {
		FeedInfo feedInfo = new FeedInfo();
		feedInfo.setUrl(url);
		this.feedInfo = MediaManager.feedInfoService.initFeedInfo(feedInfo);
	}
	
	
	public List<TorrentInfo> getTorrents() { // this includes all torrents in the DB, and fetched torrents from the rss feed
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
			try {
				refreshFeed();
				if (!feedInfo.getInitilised()) {
					feedInfo.setInitilised(true);
					saveFeedInfo();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.log(Level.INFO, "[DEBUG] Fetched feed: " + feedInfo.getName() + ", "
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
        		torrentStatus = TorrentInfo.STATUS_SKIPPED; // skip existing feed entries
        	}
        	
        	TorrentInfo newTorrent = new TorrentInfo(
				entry.getTitle(),
				entry.getLink(),
				dateAdded,
				expandedData,
				torrentStatus
    		);
        	
        	recordTorrentFromFeed(newTorrent);
        	torrentsFromFeed.add(newTorrent);
			isFeedCurrent = true;
        }
	}

	private SyndFeed getFeedXml() {
        XmlReader reader = null;
        SyndFeed feed = null;
        try {
            reader = new XmlReader(new URL(feedInfo.getUrl()));
			SyndFeedInput in = new SyndFeedInput();
			in.setPreserveWireFeed(true);
            feed = in.build(reader);
        } catch (Exception ex) {
			// TODO Auto-generated catch block
        	ex.printStackTrace();
        } finally {
	        if (reader != null) {
	        	try {
	        		reader.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
		return feed;
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
    		if (filterAttribute.getFilterType().equalsIgnoreCase(filterType) && value.matches(filterAttribute.getFilterRegex())) {
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
	
	public String toString() {
        return "FeedProvider: ttl [" + ttl + "], lastFetched [" + lastFetched + "], feedInfo [" + feedInfo + "]";
        // upload limit, finish dir, etc
    }
}
