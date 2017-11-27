package swordfishsync.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;

public class FeedProviderUtils {

	public static String getTorrentDetailsUrl(FeedProvider feedProvider, Torrent torrent) {
		String detailsUrl = torrent.getDetailsUrl();

		// check if feed has custom notification link url defined
		if (feedProvider.getDetailsUrlFormat() != null && feedProvider.getDetailsUrlValueFromRegex() != null) {
			Pattern pUrl = Pattern.compile(feedProvider.getDetailsUrlValueFromRegex());
			if (pUrl != null && torrent.getUrl() != null) {
				Matcher mUrl = pUrl.matcher(torrent.getUrl());
				if (mUrl.matches()) {
					detailsUrl = feedProvider.getDetailsUrlFormat().replace("{regex-value}", mUrl.group(1));
				}
			}
		}
		
		return detailsUrl;
	}
	public static String getTorrentDetailsUrl(TorrentState torrentState) {
		return getTorrentDetailsUrl(torrentState.getFeedProvider(), torrentState.getTorrent());
	}
	
}
