package mymedia.services.model;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.model.Episode;
import com.omertron.thetvdbapi.model.Series;
import com.omertron.themoviedbapi.*;
import com.omertron.themoviedbapi.model.MovieDb;
//import com.omertron.themoviedbapi.TheMovieDbApi;
//import com.omertron.themoviedbapi.MovieDbException;




import mymedia.db.form.TorrentInfo;

public class MediaInfo {
	
	public static final String TYPE_DEFAULT = "default";
	public static final String TYPE_TV = "tv";
	public static final String TYPE_MOVIE = "movie";
	
	private static final String NAME_KEY = "showname";
	private static final String EPISODE_ID_KEY = "episode";
	
	public static String tvdbApiKey;
	public static String tmdbApiKey;
	public static String tvdbApiNotice;
	public static String tmdbApiNotice;
	
	private TorrentInfo torrentInfo;
	private boolean fetchDetails;
	
	private String type = TYPE_DEFAULT; // tv/movie/default used for email type
	public String subDirectory = ""; // store directory under the download directory
	private String name;
	
	// common
	public String posterUrl = "";
	public String backdropUrl = "";
	public String extraInfo = "";
	public String url = ""; // this variable is used in the notification email (and torrent listing?)
	public String notice = "";
	
	// tv
	public String episodeId = "";
	public String seasonNumber = "";
	public String episodeNumber = "";
	public String episodeTitle = "";
	public String episodeDescription = "";
	
	// movie
	public String year = "";
	public String quality = "";
	public boolean hd = false;
	
	public MediaInfo(FeedProvider feedProvider, TorrentInfo torrentInfo) {
		this(feedProvider, torrentInfo, true);
	}

	public MediaInfo(FeedProvider feedProvider, TorrentInfo torrentInfo, boolean fetchDetails) {
		this.torrentInfo = torrentInfo;
		this.fetchDetails = fetchDetails;
		url = feedProvider.getTorrentDetailsUrl(torrentInfo);
		
		// default values
		name = torrentInfo.getName();
		subDirectory = name + "/";
		
		processMedia(feedProvider, torrentInfo);
	}
	
	public void processMedia(FeedProvider feedProvider, TorrentInfo torrentInfo) {
		
		// determine if torrent is tv/movie/other
		
		// make regex customisable (app settings), ability to add regex checks to categories
		/*
		String[] tests = {

		};
		//Pattern pTest = Pattern.compile("^([^-]+)[\\.|_ ](\\d{4}).*\\.(.{2,4}).*");
		Pattern pTest = Pattern.compile("^(.*[^-]+)[\\.|_ ](\\d{4}).*\\.(\\d{3,4}p).*\\.(.{2,4}).*"); // hd
		//Pattern pTest = Pattern.compile("^([^-]+)[\\.|_ ](\\d{4}).*\\.(.{2,4}).*"); // sd
		for (String s : tests) {
			Matcher m2 = pTest.matcher(s);
			if (m2.matches()) {
			    System.out.printf("Title: %-23s Year: %s Resolution: %s%n",
			            m2.group(1), m2.group(2), m2.group(3));
			    //System.out.println(m2.group(4));
			} else {
		    	System.out.println("[DEBUG] string: " + s);
		    	System.out.println(m2);
			}
		}
		*/
		/*
		String[] tests2 = {
		
		};
		Pattern pTest2 = Pattern.compile("(.*?)[.\\s][sS](\\d{2})[eE](\\d{2}).*");
		for (String s : tests2) {
		    Matcher m2 = pTest2.matcher(s);
		    if (m2.matches()) {
		        System.out.printf("Name: %-23s Season: %s Episode: %s%n",
		                m2.group(1).replace(".", " "), m2.group(2), m2.group(3));
		    }
		    else {
		    	System.out.println("[DEBUG] string: " + s);
		    	System.out.println(m2);
		    }
		}
		*/
		
		
		// need to check tv shows 1x01, 01x01
		//System.out.println("[DEBUG] #### MediaInfo TV show check 1x01");
		
		
		//System.out.println("[DEBUG] #### MediaInfo TV show check S##E##");
		// TV show regex S##E##
		Pattern pTv = Pattern.compile("(.*?)[.\\s][sS](\\d{2})[eE](\\d{2}).*");
		Matcher mTv = pTv.matcher(torrentInfo.getName());
		if (mTv.matches()) {
			/*System.out.println("[DEBUG] #### MediaInfo TV show matched S##E##");
			System.out.println("[DEBUG] #### mTvx.group(1) name: " + mTv.group(1));
			System.out.println("[DEBUG] #### mTvx.group(2) seasonNumber: " + mTv.group(2));
			System.out.println("[DEBUG] #### mTvx.group(3) episodeNumber: " + mTv.group(3));*/
			name = mTv.group(1).replace(".", " ");
			seasonNumber = mTv.group(2);
			episodeNumber = mTv.group(3);
			type = TYPE_TV;
			processTvShow();
			return;
		}
		
		//System.out.println("[DEBUG] #### MediaInfo TV show check ##x###");
		// TV show regex ##x###
		Pattern pTvx = Pattern.compile("(.*?)(\\d+)x(\\d+).*");
		Matcher mTvx = pTvx.matcher(torrentInfo.getName());
		if (mTvx.matches()) {
			/*System.out.println("[DEBUG] #### MediaInfo TV show matched ##x###");
			System.out.println("[DEBUG] #### mTvx.group(1) name: " + mTvx.group(1));
			System.out.println("[DEBUG] #### mTvx.group(2) seasonNumber: " + mTvx.group(2));
			System.out.println("[DEBUG] #### mTvx.group(3) episodeNumber: " + mTvx.group(3));*/
			name = mTvx.group(1).replace(".", " ");
			seasonNumber = mTvx.group(2);
			episodeNumber = mTvx.group(3);
			type = TYPE_TV;
			processTvShow();
			return;
		}
		
		//System.out.println("[DEBUG] #### HDTV check");
		// might be a non standard tv show format
		if (torrentInfo.getName().toLowerCase().contains("hdtv")) {
			processTvShow();
			return;
		}
		
		
		//System.out.println("[DEBUG] #### MediaInfo HD movie check");
		// HD movie regex
		Pattern pMovieHd = Pattern.compile("^(.*[^-]+)[\\.|_ ](\\d{4}).*\\.(\\d{3,4}p).*\\.(.{2,4}).*");
		Matcher mMovieHd = pMovieHd.matcher(torrentInfo.getName());
		if (mMovieHd.matches()) {
			name = mMovieHd.group(1).replace(".", " ");
			year = mMovieHd.group(2);
			quality = mMovieHd.group(3);
			type = TYPE_MOVIE;
			hd = true; // determine if high definition; use the sub directory "Movies (HD)"... to be implemented
			processMovie();
			return;
		}
		
		//System.out.println("[DEBUG] #### MediaInfo SD movie check");
		// SD movie regex
		Pattern pMovieSd = Pattern.compile("^([^-]+)[\\.|_ ](\\d{4}).*\\.(.{2,4}).*");
		Matcher mMovieSd = pMovieSd.matcher(torrentInfo.getName());
		if (mMovieSd.matches()) {
			name = mMovieSd.group(1).replace(".", " ");
			year = mMovieSd.group(2);
			quality = mMovieSd.group(3);
			type = TYPE_MOVIE;
			processMovie();
			return;
		}
		
		//System.out.println("[DEBUG] #### no type matched ##############");
	}
	
	private void processMovie() {
		if (StringUtils.isNotBlank(tmdbApiKey) && StringUtils.isNotBlank(name) && StringUtils.isNotBlank(year)) {
			notice = tmdbApiNotice;
			
			determineMovieSubDirectory();

			if (fetchDetails) {
				try {
					TheMovieDbApi tmdb = new TheMovieDbApi(tmdbApiKey);
					
					//String imdbId = "";
					int movieId;
					
					List<MovieDb> searchResults = tmdb.searchMovie(name, Integer.parseInt(year), null, true, 0);
					
					for (MovieDb searchResult : searchResults) {
						// some image width sizes: w185, w342, w1280
						if (StringUtils.isNotBlank(searchResult.getBackdropPath())) {
							backdropUrl = "http://cf2.imgobject.com/t/p/w342/" + searchResult.getBackdropPath(); // save this url in properties
						}
						if (StringUtils.isNotBlank(searchResult.getPosterPath())) {
							posterUrl = "http://cf2.imgobject.com/t/p/w342/" + searchResult.getPosterPath();
						}
			        	if (backdropUrl.equalsIgnoreCase(posterUrl)) {
			        		posterUrl = "";
			        	}
						//imdbId = searchResult.getImdbID();
						movieId = searchResult.getId();
						MovieDb movieInfo = tmdb.getMovieInfo(movieId, null);
						extraInfo = movieInfo.getOverview();
						break; // need to handle multiple results, only using the first one...
					}
				} catch (MovieDbException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}
	}
	
	private void processTvShow() {
		if (tvdbApiKey != null && !tvdbApiKey.isEmpty()) {
			notice = tvdbApiNotice;
			boolean fetchTvdb = true;
			
			if (torrentInfo.getProperties() != null) {
				for (Map.Entry<String, String> expandedData : torrentInfo.getProperties().entrySet()) {
					switch (expandedData.getKey()) {
						case NAME_KEY:
							name = expandedData.getValue();
							break;
						case EPISODE_ID_KEY:
							episodeId = expandedData.getValue();
							break;
					}
				}
			}
			if (StringUtils.isBlank(name)) {
				name = torrentInfo.getName();
				fetchTvdb = false;
			} else {
				determineTvSubDirectory();
			}
			
			if (fetchDetails && fetchTvdb) {
				try {
					TheTVDBApi tvDB = new TheTVDBApi(tvdbApiKey);
					
					String showId = null;
					for (Series series : tvDB.searchSeries(name, null)) {
						posterUrl = series.getBanner();
						extraInfo = series.getOverview();
						showId = series.getId();
						break; // need to handle multiple results, only using the first one...
					}

					if (StringUtils.isNotBlank(showId) && StringUtils.isNotBlank(seasonNumber) && StringUtils.isNotBlank(episodeNumber)) {
						Episode episode = tvDB.getEpisode(showId, Integer.parseInt(seasonNumber), Integer.parseInt(episodeNumber), "en");
						if (episode != null) {
							if (StringUtils.isNotBlank(episode.getFilename())) {
								backdropUrl = episode.getFilename();
					        	if (backdropUrl.equalsIgnoreCase(posterUrl)) {
					        		posterUrl = "";
					        	}
							}
							if (StringUtils.isNotBlank(episode.getEpisodeName())) {
								episodeTitle = episode.getEpisodeName();
							}
							if (StringUtils.isNotBlank(episode.getOverview())) {
								episodeDescription = episode.getOverview();
							}
						}
					}
				} catch (Exception e) {
					System.out.println("TEST - MediaInfo.processTvShow()");
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void determineTvSubDirectory() {
		String seasonDirectoryPrefix = "Season"; // should be a property - seasonDirectoryPrefix
		subDirectory = name + "/";
		if (StringUtils.isNotBlank(seasonNumber)) {
			if (seasonNumber.length() == 1) {
				subDirectory = subDirectory + seasonDirectoryPrefix + " 0" + seasonNumber + "/";
			} else {
				subDirectory = subDirectory + seasonDirectoryPrefix + " " + seasonNumber + "/";
			}
		}
	}
	
	private void determineMovieSubDirectory() {
		subDirectory = name + " (" + year + ") (" + quality + ")/";
	}
	
	public String getType() {
		return type;
	}
	public String getName() {
		return name;
	}
	
   	public String toString() {
        return "MediaInfo: type [" + type + "], name [" + name + "], seasonNumber [" + seasonNumber + "], episodeNumber [" + episodeNumber + "]";
   	}
   	
}
