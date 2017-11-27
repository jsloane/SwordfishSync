package swordfishsync.model;

public class TorrentContent {

	public enum Type {
		DEFAULT, TV, MOVIE
		// todo: MUSIC
	}
	
	Type type = Type.DEFAULT; // tv/movie/default used for email type
	String subDirectory; // store directory under the download directory
	String downloadDirectory;
	String name;
	
	// common
	String posterUrl;
	String backdropUrl;
	String extraInfo;
	String detailsUrl; // this variable is used in the notification email
	String notice;
	
	// tv
	String episodeId;
	String seasonNumber;
	String episodeNumber;
	String episodeTitle;
	String episodeDescription;
	Boolean proper = false;
	Boolean repack = false;
	Boolean real = false;
	
	// movie
	String year;
	String quality;
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public String getSubDirectory() {
		return subDirectory;
	}
	public void setSubDirectory(String subDirectory) {
		this.subDirectory = subDirectory;
	}
	public String getDownloadDirectory() {
		return downloadDirectory;
	}
	public void setDownloadDirectory(String downloadDirectory) {
		this.downloadDirectory = downloadDirectory;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPosterUrl() {
		return posterUrl;
	}
	public void setPosterUrl(String posterUrl) {
		this.posterUrl = posterUrl;
	}
	public String getBackdropUrl() {
		return backdropUrl;
	}
	public void setBackdropUrl(String backdropUrl) {
		this.backdropUrl = backdropUrl;
	}
	public String getExtraInfo() {
		return extraInfo;
	}
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	public String getDetailsUrl() {
		return detailsUrl;
	}
	public void setDetailsUrl(String detailsUrl) {
		this.detailsUrl = detailsUrl;
	}
	public String getNotice() {
		return notice;
	}
	public void setNotice(String notice) {
		this.notice = notice;
	}
	public String getEpisodeId() {
		return episodeId;
	}
	public void setEpisodeId(String episodeId) {
		this.episodeId = episodeId;
	}
	public String getSeasonNumber() {
		return seasonNumber;
	}
	public void setSeasonNumber(String seasonNumber) {
		this.seasonNumber = seasonNumber;
	}
	public String getEpisodeNumber() {
		return episodeNumber;
	}
	public void setEpisodeNumber(String episodeNumber) {
		this.episodeNumber = episodeNumber;
	}
	public String getEpisodeTitle() {
		return episodeTitle;
	}
	public void setEpisodeTitle(String episodeTitle) {
		this.episodeTitle = episodeTitle;
	}
	public String getEpisodeDescription() {
		return episodeDescription;
	}
	public void setEpisodeDescription(String episodeDescription) {
		this.episodeDescription = episodeDescription;
	}
	public Boolean getProper() {
		return proper;
	}
	public void setProper(Boolean proper) {
		this.proper = proper;
	}
	public Boolean getRepack() {
		return repack;
	}
	public void setRepack(Boolean repack) {
		this.repack = repack;
	}
	public Boolean getReal() {
		return real;
	}
	public void setReal(Boolean real) {
		this.real = real;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getQuality() {
		return quality;
	}
	public void setQuality(String quality) {
		this.quality = quality;
	}
	
}
