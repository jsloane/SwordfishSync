package mymedia.services.model;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private String type = TYPE_DEFAULT; // tv/movie/default used for email type
	public String subDirectory = ""; // store directory under the download directory
	private String name;
	
	// common
	public String posterUrl = "";
	public String backdropUrl = "";
	public String extraInfo = "";
	public String url = "";
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
	
	
	public MediaInfo(TorrentInfo torrentInfo) {
		this.torrentInfo = torrentInfo;
		url = torrentInfo.getUrl();
		
		// default values
		name = torrentInfo.getName();
		subDirectory = name + "/";
		
		// determine if torrent is tv/movie/other
		
		
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
		
		
		
		//System.out.println("[DEBUG] #### MediaInfo TV show check");
		// TV show regex
		Pattern pTv = Pattern.compile("(.*?)[.\\s][sS](\\d{2})[eE](\\d{2}).*");
		Matcher mTv = pTv.matcher(torrentInfo.getName());
		if (mTv.matches()) {
			name = mTv.group(1).replace(".", " ");
			seasonNumber = mTv.group(2);
			episodeNumber = mTv.group(3);
			type = TYPE_TV;
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
			hd = true; // determine if high definition; use the sub directory "Movies (HD)"...
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
		
		//System.out.println("[DEBUG] #### HDTV check");
		// might be a non standard tv show format
		if (torrentInfo.getName().toLowerCase().contains("hdtv")) {
			processTvShow();
			return;
		}
		
		//System.out.println("[DEBUG] #### no type matched ##############");
	}
	
	private void processMovie() {
		if (tmdbApiKey != null && !tmdbApiKey.isEmpty()
				&& name != null && !name.trim().isEmpty()
				&& year != null && !year.trim().isEmpty()
				) {
			notice = tmdbApiNotice;
			
			determineMovieSubDirectory();
			
			try {
				TheMovieDbApi tmdb = new TheMovieDbApi(tmdbApiKey);
				
				//String imdbId = "";
				int movieId;
				
				List<MovieDb> searchResults = tmdb.searchMovie(name, Integer.parseInt(year), null, true, 0);
				
				for (MovieDb searchResult : searchResults) {
					// some image width sizes: w185, w342, w1280
					if (searchResult.getBackdropPath() != null && !searchResult.getBackdropPath().isEmpty()) {
						backdropUrl = "http://cf2.imgobject.com/t/p/w342/" + searchResult.getBackdropPath();
					}
					if (searchResult.getPosterPath() != null && !searchResult.getPosterPath().isEmpty()) {
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
	
	private void processTvShow() {
		if (tvdbApiKey != null && !tvdbApiKey.isEmpty()) {
			notice = tvdbApiNotice;
			boolean fetchTvdb = true;

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
			if (!(name != null && !name.trim().isEmpty())) {
				name = torrentInfo.getName();
				fetchTvdb = false;
			} else {
				determineTvSubDirectory();
			}
			
			if (fetchTvdb) {
				try {
					TheTVDBApi tvDB = new TheTVDBApi(tvdbApiKey);
					
					String showId = null;
					for (Series series : tvDB.searchSeries(name, null)) {
						posterUrl = series.getBanner();
						extraInfo = series.getOverview();
						showId = series.getId();
						break; // need to handle multiple results, only using the first one...
					}
					
					if (showId != null && !showId.isEmpty()
							&& seasonNumber != null && !seasonNumber.isEmpty()
							&& episodeNumber != null && !episodeNumber.isEmpty()) {
						Episode episode = tvDB.getEpisode(showId, Integer.parseInt(seasonNumber), Integer.parseInt(episodeNumber), "en");
						if (episode != null) {
							if (episode.getFilename() != null && !episode.getFilename().isEmpty()) {
								backdropUrl = episode.getFilename();
					        	if (backdropUrl.equalsIgnoreCase(posterUrl)) {
					        		posterUrl = "";
					        	}
							}
							if (episode.getEpisodeName() != null) {
								episodeTitle = episode.getEpisodeName();
							}
							if (episode.getOverview() != null) {
								episodeDescription = episode.getOverview();
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void determineTvSubDirectory() {
		String seasonDirectoryPrefix = "Season"; // should be a property - seasonDirectoryPrefix
		subDirectory = name + "/";
		if (!seasonNumber.isEmpty()) {
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
