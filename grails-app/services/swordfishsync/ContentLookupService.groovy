package swordfishsync

import java.util.List
import java.util.Map
import java.util.regex.Matcher
import java.util.regex.Pattern

import com.omertron.themoviedbapi.TheMovieDbApi
import com.omertron.themoviedbapi.model.MovieDb
import com.omertron.thetvdbapi.TheTVDBApi
import com.omertron.thetvdbapi.model.Episode
import com.omertron.thetvdbapi.model.Series

import grails.util.GrailsStringUtils
import groovy.util.logging.Slf4j

@Slf4j
class ContentLookupService {
	
	TorrentContent getTorrentContentInfo(FeedProvider feedProvider, Torrent torrent, String name) {
		return getTorrentContentInfo(feedProvider, torrent, name, true)
	}
	
	TorrentContent getTorrentContentInfo(FeedProvider feedProvider, Torrent torrent, String name, Boolean fetchDetails) {
		TorrentContent torrentContent = new TorrentContent(
			subDirectory:	name + System.getProperty('file.separator')//,
			//detailsUrl:			''// todo: get details url
		)
		constructDownloadDirectory(feedProvider, torrentContent)
		
		if (name.toLowerCase().contains('proper')) {
			torrentContent.proper = true
		}
		if (name.toLowerCase().contains('repack')) {
			torrentContent.repack = true
		}
		
		// determine if torrent is tv/movie/other
		
		// TV show regex S##E##
		Pattern pTv = Pattern.compile("(.*?)[.\\s][sS](\\d{2})[eE](\\d{2}).*")
		Matcher mTv = pTv.matcher(name)
		if (mTv.matches()) {
			torrentContent.name = mTv.group(1).replace(".", " ")
			torrentContent.seasonNumber = mTv.group(2)
			torrentContent.episodeNumber = mTv.group(3)
			torrentContent.type = TorrentContent.Type.TV
			processTvShow(feedProvider, torrentContent, torrent, fetchDetails)
			return torrentContent
		}
		
		// TV show regex ##x###
		Pattern pTvx = Pattern.compile("(.*?)(\\d+)x(\\d+).*")
		Matcher mTvx = pTvx.matcher(name)
		if (mTvx.matches()) {
			torrentContent.name = mTvx.group(1).replace(".", " ")
			torrentContent.seasonNumber = mTvx.group(2)
			torrentContent.episodeNumber = mTvx.group(3)
			torrentContent.type = TorrentContent.Type.TV
			processTvShow(feedProvider, torrentContent, torrent, fetchDetails)
			return torrentContent
		}
		
		// non standard tv show format
		if (name.toLowerCase().contains('hdtv')) {
			torrentContent.type = TorrentContent.Type.TV
			processTvShow(feedProvider, torrentContent, torrent, fetchDetails)
			if (!torrentContent.name) {
				torrentContent.name = name
			}
			return torrentContent
		}
		
		// HD movie regex
		Pattern pMovieHd = Pattern.compile("^(.*[^-]+)[\\.|_ ](\\d{4}).*\\.(\\d{3,4}p).*\\.(.{2,4}).*")
		Matcher mMovieHd = pMovieHd.matcher(name)
		if (mMovieHd.matches()) {
			torrentContent.name = mMovieHd.group(1).replace(".", " ")
			torrentContent.year = mMovieHd.group(2)
			torrentContent.quality = mMovieHd.group(3)
			torrentContent.type = TorrentContent.Type.MOVIE
			processMovie(feedProvider, torrentContent, fetchDetails)
			return torrentContent
		}
		
		// SD movie regex
		Pattern pMovieSd = Pattern.compile("^([^-]+)[\\.|_ ](\\d{4}).*\\.(.{2,4}).*")
		Matcher mMovieSd = pMovieSd.matcher(name)
		if (mMovieSd.matches()) {
			torrentContent.name = mMovieSd.group(1).replace(".", " ")
			torrentContent.year = mMovieSd.group(2)
			torrentContent.quality = mMovieSd.group(3)
			torrentContent.type = TorrentContent.Type.MOVIE
			processMovie(feedProvider, torrentContent, fetchDetails)
			return torrentContent
		}
		
		// other
		torrentContent.name = name
		return torrentContent
	}
	
	private def processTvShow(FeedProvider feedProvider, TorrentContent torrentContent, Torrent torrent, Boolean fetchDetails) {
		println '### processTvShow ###'
		String tvdbApiKey = 'C43C7180A2A8E155' // get from setting
		String tvdbApiNotice = '' // get from setting
		if (tvdbApiKey != null && !tvdbApiKey.isEmpty()) {
			torrentContent.notice = ''//tvdbApiNotice // get from setting
			
			if (torrent?.expandedData) {
				torrent.expandedData.each { expandedData ->
					switch (expandedData.name) {
						case TorrentContent.NAME_KEY:
							torrentContent.name = expandedData.value
							break
						case TorrentContent.EPISODE_ID_KEY:
							torrentContent.episodeId = expandedData.value
							break
					}
				}
			}
			
			if (fetchDetails && torrentContent.name) {
				try {
					TheTVDBApi tvDB = new TheTVDBApi(tvdbApiKey);
					
					String showId = null;
					for (Series series : tvDB.searchSeries(torrentContent.name, null)) {
						torrentContent.posterUrl = series.getBanner()
						torrentContent.extraInfo = series.getOverview()
						showId = series.getId()
						break; // need to handle multiple results, only using the first one...
						// fix this, eg cops - https://kat.cr/cops-s24e15-hdtv-xvid-avi-t12325426.html
					}
					
					if (GrailsStringUtils.isNotBlank(showId) && GrailsStringUtils.isNotBlank(torrentContent.seasonNumber) && GrailsStringUtils.isNotBlank(torrentContent.episodeNumber)) {
						Episode episode = tvDB.getEpisode(showId, Integer.parseInt(torrentContent.seasonNumber), Integer.parseInt(torrentContent.episodeNumber), "en")
						if (episode != null) {
							if (GrailsStringUtils.isNotBlank(episode.getFilename())) {
								torrentContent.backdropUrl = episode.getFilename()
								if (torrentContent.backdropUrl.equalsIgnoreCase(torrentContent.posterUrl)) {
									torrentContent.posterUrl = ''
								}
							}
							if (GrailsStringUtils.isNotBlank(episode.getEpisodeName())) {
								torrentContent.episodeTitle = episode.getEpisodeName()
							}
							if (GrailsStringUtils.isNotBlank(episode.getOverview())) {
								torrentContent.episodeDescription = episode.getOverview()
							}
						}
					}
				} catch (Exception e) {
					log.error('Error fetching TV show details', e)
				}
			}
		}
		
		determineTvSubDirectory(feedProvider, torrentContent)
	}
	
	private def processMovie(FeedProvider feedProvider, TorrentContent torrentContent, Boolean fetchDetails) {
		println '### processMovie ###'
		String tmdbApiKey = '5a1a77e2eba8984804586122754f969f' // get from setting
		String tmdbApiNotice = '' // get from setting
		if (GrailsStringUtils.isNotBlank(tmdbApiKey) && GrailsStringUtils.isNotBlank(torrentContent.name) && GrailsStringUtils.isNotBlank(torrentContent.year)) {
			torrentContent.notice = tmdbApiNotice
			
			if (fetchDetails) {
				try {
					TheMovieDbApi tmdb = new TheMovieDbApi(tmdbApiKey);
					
					int movieId;
					
					List<MovieDb> searchResults = tmdb.searchMovie(torrentContent.name, Integer.parseInt(torrentContent.year), null, true, 0).getResults()
					
					for (MovieDb searchResult : searchResults) {
						// some image width sizes: w185, w342, w1280
						if (GrailsStringUtils.isNotBlank(searchResult.getBackdropPath())) {
							torrentContent.backdropUrl = 'http://cf2.imgobject.com/t/p/w342/' + searchResult.getBackdropPath() // todo: save this url in properties
						}
						if (GrailsStringUtils.isNotBlank(searchResult.getPosterPath())) {
							torrentContent.posterUrl = 'http://cf2.imgobject.com/t/p/w342/' + searchResult.getPosterPath()
						}
						if (torrentContent.backdropUrl.equalsIgnoreCase(torrentContent.posterUrl)) {
							torrentContent.posterUrl = ''
						}
						movieId = searchResult.getId()
						MovieDb movieInfo = tmdb.getMovieInfo(movieId, null)
						torrentContent.extraInfo = movieInfo.getOverview()
						break // need to handle multiple results, only using the first one...
					}
				} catch (Exception e) {
					log.error('Error fetching Movie details', e)
				}
			}
		}
		
		determineMovieSubDirectory(feedProvider, torrentContent)
	}
	
	def determineTvSubDirectory(FeedProvider feedProvider, TorrentContent torrentContent) {
		String seasonDirectoryPrefix = 'Season' // should be a property - seasonDirectoryPrefix
		if (torrentContent.name) {
			torrentContent.subDirectory = torrentContent.name + System.getProperty('file.separator')
			if (GrailsStringUtils.isNotBlank(torrentContent.seasonNumber)) {
				if (torrentContent.seasonNumber.length() == 1) {
					torrentContent.subDirectory = torrentContent.subDirectory + seasonDirectoryPrefix + ' 0' + torrentContent.seasonNumber + System.getProperty('file.separator')
				} else {
					torrentContent.subDirectory = torrentContent.subDirectory + seasonDirectoryPrefix + ' ' + torrentContent.seasonNumber + System.getProperty('file.separator')
				}
			}
		}
		constructDownloadDirectory(feedProvider, torrentContent)
	}
	
	def determineMovieSubDirectory(FeedProvider feedProvider, TorrentContent torrentContent) {
		torrentContent.subDirectory = torrentContent.name + ' (' + torrentContent.year + ') (' + torrentContent.quality + ')' + System.getProperty('file.separator')
		constructDownloadDirectory(feedProvider, torrentContent)
	}
	
	private def constructDownloadDirectory(FeedProvider feedProvider, TorrentContent torrentContent) {
		String baseDir = feedProvider.downloadDirectory
		
		if (baseDir != null && feedProvider.determineSubDirectory && !torrentContent.subDirectory.equals(System.getProperty('file.separator'))) {
			baseDir = baseDir + torrentContent.subDirectory
		}
		
		torrentContent.downloadDirectory = baseDir
	}
	
}
