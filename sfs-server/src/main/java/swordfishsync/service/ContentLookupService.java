package swordfishsync.service;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;
import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.model.Episode;
import com.omertron.thetvdbapi.model.Series;

import swordfishsync.domain.ExpandedData;
import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.Torrent;
import swordfishsync.model.TorrentContent;
import swordfishsync.util.FeedProviderUtils;

@Service("contentLookupService")
public class ContentLookupService {

    private static final Logger log = LoggerFactory.getLogger(ContentLookupService.class);

	@Resource
	SettingService settingService;
    
	private static final String NAME_KEY = "showname";
	private static final String EPISODE_ID_KEY = "episode";
	
	TorrentContent getTorrentContentInfo(FeedProvider feedProvider, Torrent torrent) {
		return getTorrentContentInfo(feedProvider, torrent, torrent.getName(), true);
	}

	TorrentContent getTorrentContentInfo(FeedProvider feedProvider, Torrent torrent, String name, boolean fetchDetails) {

		TorrentContent torrentContent = new TorrentContent();
		torrentContent.setSubDirectory(name + System.getProperty("file.separator"));
		torrentContent.setDetailsUrl(FeedProviderUtils.getTorrentDetailsUrl(feedProvider, torrent));
		constructDownloadDirectory(feedProvider, torrentContent);
		
		if (name.toLowerCase().contains("proper")) {
			torrentContent.setProper(true);
		}
		if (name.toLowerCase().contains("repack")) {
			torrentContent.setRepack(true);
		}
		
		// determine if torrent is tv/movie/other
		
		// TV show regex S##E##
		Pattern pTv = Pattern.compile("(.*?)[.\\s][sS](\\d{2})[eE](\\d{2}).*");
		Matcher mTv = pTv.matcher(name);
		if (mTv.matches()) {
			torrentContent.setName(mTv.group(1).replace(".", " "));
			torrentContent.setSeasonNumber(mTv.group(2));
			torrentContent.setEpisodeNumber(mTv.group(3));
			torrentContent.setType(TorrentContent.Type.TV);
			processTvShow(feedProvider, torrentContent, torrent, fetchDetails);
			return torrentContent;
		}
		
		// TV show regex ##x###
		Pattern pTvx = Pattern.compile("(.*?)(\\d+)x(\\d+).*");
		Matcher mTvx = pTvx.matcher(name);
		if (mTvx.matches()) {
			torrentContent.setName(mTvx.group(1).replace(".", " "));
			torrentContent.setSeasonNumber(mTvx.group(2));
			torrentContent.setEpisodeNumber(mTvx.group(3));
			torrentContent.setType(TorrentContent.Type.TV);
			processTvShow(feedProvider, torrentContent, torrent, fetchDetails);
			return torrentContent;
		}
		
		// non standard tv show format
		if (name.toLowerCase().contains("hdtv")) {
			torrentContent.setType(TorrentContent.Type.TV);
			processTvShow(feedProvider, torrentContent, torrent, fetchDetails);
			if (torrentContent.getName() == null || torrentContent.getName().isEmpty()) { // todo - use string utils to check null
				torrentContent.setName(name);
			}
			return torrentContent;
		}
		
		// HD movie regex
		Pattern pMovieHd = Pattern.compile("^(.*[^-]+)[\\.|_ ](\\d{4}).*\\.(\\d{3,4}p).*\\.(.{2,4}).*");
		Matcher mMovieHd = pMovieHd.matcher(name);
		if (mMovieHd.matches()) {
			torrentContent.setName(mMovieHd.group(1).replace(".", " "));
			torrentContent.setYear(mMovieHd.group(2));
			torrentContent.setQuality(mMovieHd.group(3));
			torrentContent.setType(TorrentContent.Type.MOVIE);
			processMovie(feedProvider, torrentContent, fetchDetails);
			return torrentContent;
		}
		
		// SD movie regex
		Pattern pMovieSd = Pattern.compile("^([^-]+)[\\.|_ ](\\d{4}).*\\.(.{2,4}).*");
		Matcher mMovieSd = pMovieSd.matcher(name);
		if (mMovieSd.matches()) {
			torrentContent.setName(mMovieSd.group(1).replace(".", " "));
			torrentContent.setYear(mMovieSd.group(2));
			torrentContent.setQuality(mMovieSd.group(3));
			torrentContent.setType(TorrentContent.Type.MOVIE);
			processMovie(feedProvider, torrentContent, fetchDetails);
			return torrentContent;
		}
		
		// other
		torrentContent.setName(name);
		return torrentContent;
	}

	private void processTvShow(FeedProvider feedProvider, TorrentContent torrentContent, Torrent torrent, boolean fetchDetails) {
		//println '### processTvShow ###'
		String tvdbApiKey = settingService.getValue(SettingService.CODE_MEDIA_TVDB_APIKEY, String.class);
		if (tvdbApiKey != null && !tvdbApiKey.isEmpty()) {
			//torrentContent.setNotice(tvdbApiNotice);
			
			if (torrent != null && torrent.getExpandedData() != null) {
				for (ExpandedData expandedData : torrent.getExpandedData()) {
					switch (expandedData.getName()) {
						case NAME_KEY:
							torrentContent.setName(expandedData.getValue());
							break;
						case EPISODE_ID_KEY:
							torrentContent.setEpisodeId(expandedData.getValue());
							break;
					}
				}
			}
			
			if (fetchDetails && StringUtils.isNotBlank(torrentContent.getName())) {
				try {
					TheTVDBApi tvDB = new TheTVDBApi(tvdbApiKey);
					
					String showId = null;
					String seriesName = null;
					List<Series> searchResults = tvDB.searchSeries(torrentContent.getName(), null);
					if (searchResults != null) {
						for (Series series : searchResults) {
							torrentContent.setPosterUrl(series.getBanner());
							torrentContent.setExtraInfo(series.getOverview());
							seriesName = series.getSeriesName();
							showId = series.getId();
							break; // todo - need to handle multiple results, only using the first one...
						}
					}
					
					if (StringUtils.isNotBlank(showId) && StringUtils.isNotBlank(torrentContent.getSeasonNumber()) && StringUtils.isNotBlank(torrentContent.getEpisodeNumber())) {
						Episode episode = tvDB.getEpisode(
								showId, Integer.parseInt(torrentContent.getSeasonNumber()), Integer.parseInt(torrentContent.getEpisodeNumber()), "en"
						);
						if (episode != null) {
							if (StringUtils.isNotBlank(seriesName)) {
								//torrentContent.setName(seriesName);
							}
							if (StringUtils.isNotBlank(episode.getFilename())) {
								torrentContent.setBackdropUrl(episode.getFilename());
								if (torrentContent.getBackdropUrl().equalsIgnoreCase(torrentContent.getPosterUrl())) {
									torrentContent.setPosterUrl("");
								}
							}
							if (StringUtils.isNotBlank(episode.getEpisodeName())) {
								torrentContent.setEpisodeTitle(episode.getEpisodeName());
							}
							if (StringUtils.isNotBlank(episode.getOverview())) {
								torrentContent.setEpisodeDescription(episode.getOverview());
							}
						}
					}
				} catch (Exception e) {
					log.error("Error fetching TV show details for [" + torrentContent.getName() + "]", e);
				}
			}
		}
		
		determineTvSubDirectory(feedProvider, torrentContent);
	}

	private void processMovie(FeedProvider feedProvider, TorrentContent torrentContent, boolean fetchDetails) {
		//println '### processMovie ###'
		String tmdbApiKey = settingService.getValue(SettingService.CODE_MEDIA_TMDB_APIKEY, String.class);
		if (StringUtils.isNotBlank(tmdbApiKey) && StringUtils.isNotBlank(torrentContent.getName()) && StringUtils.isNotBlank(torrentContent.getYear())) {
			//torrentContent.setNotice(tmdbApiNotice);
			
			if (fetchDetails) {
				try {
					TheMovieDbApi tmdb = new TheMovieDbApi(tmdbApiKey);
					
					int movieId;
					ResultList<MovieInfo> tmdbResultsList = tmdb.searchMovie(
							torrentContent.getName(), null, "en", true, Integer.parseInt(torrentContent.getYear()), null, null
					);
					if (tmdbResultsList != null) {
						List<MovieInfo> searchResults = tmdbResultsList.getResults();
						if (searchResults != null) {
							for (MovieInfo searchResult : searchResults) {
								// some image width sizes: w185, w342, w1280
								if (StringUtils.isNotBlank(searchResult.getBackdropPath())) {
									torrentContent.setBackdropUrl("http://cf2.imgobject.com/t/p/w342/" + searchResult.getBackdropPath()); // todo: save this url in properties
								}
								if (StringUtils.isNotBlank(searchResult.getPosterPath())) {
									torrentContent.setPosterUrl("http://cf2.imgobject.com/t/p/w342/" + searchResult.getPosterPath()); // todo: save this url in properties
								}
								if (torrentContent.getBackdropUrl() != null && torrentContent.getBackdropUrl().equalsIgnoreCase(torrentContent.getPosterUrl())) {
									torrentContent.setPosterUrl("");
								}
								movieId = searchResult.getId();
								MovieInfo movieInfo = tmdb.getMovieInfo(movieId, null);
								torrentContent.setExtraInfo(movieInfo.getOverview());
								break; // todo - need to handle multiple results, only using the first one...
							}
						}
					}
				} catch (Exception e) {
					log.error("Error fetching Movie details for [" + torrentContent.getName() + "]", e);
				}
			}
		}
		
		determineMovieSubDirectory(feedProvider, torrentContent);
	}

	private void determineMovieSubDirectory(FeedProvider feedProvider, TorrentContent torrentContent) {
		torrentContent.setSubDirectory(torrentContent.getName() + " (" + torrentContent.getYear() + ") (" + torrentContent.getQuality() + ")" + System.getProperty("file.separator"));
		constructDownloadDirectory(feedProvider, torrentContent);
	}

	private void determineTvSubDirectory(FeedProvider feedProvider, TorrentContent torrentContent) {
		String seasonDirectoryPrefix = "Season"; // todo - could be a property - seasonDirectoryPrefix
		if (StringUtils.isNotBlank(torrentContent.getName())) {
			torrentContent.setSubDirectory(torrentContent.getName());

			// determine if directory already exists, ignoring case
			if (feedProvider.getDownloadDirectory() != null) {
				File feedProviderDownloadDir = new File(feedProvider.getDownloadDirectory());
				if (feedProviderDownloadDir.exists()) {
					for (String downloadDirFile : feedProviderDownloadDir.list()) {
						if (downloadDirFile.equalsIgnoreCase(torrentContent.getSubDirectory())) {
							torrentContent.setSubDirectory(downloadDirFile);
							break;
						}
					}
				}
			}
			torrentContent.setSubDirectory(torrentContent.getSubDirectory() + System.getProperty("file.separator"));

			if (StringUtils.isNotBlank(torrentContent.getSeasonNumber())) {
				String seasonNumberPrefix = " ";
				if (torrentContent.getSeasonNumber().length() == 1) {
					seasonNumberPrefix = " 0";
				}
				torrentContent.setSubDirectory(
						torrentContent.getSubDirectory() + seasonDirectoryPrefix + seasonNumberPrefix + torrentContent.getSeasonNumber() + System.getProperty("file.separator")
				);
			}
		}
		constructDownloadDirectory(feedProvider, torrentContent);
	}

	private void constructDownloadDirectory(FeedProvider feedProvider, TorrentContent torrentContent) {
		String baseDir = feedProvider.getDownloadDirectory();
		
		if (baseDir != null && feedProvider.getDetermineSubDirectory() != null && !torrentContent.getSubDirectory().equals(System.getProperty("file.separator"))) {
			baseDir = baseDir + torrentContent.getSubDirectory();
		}

		torrentContent.setDownloadDirectory(baseDir);
	}
	
}
