package swordfishsync.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TorrentDetails {

	public enum Status {
		UNKNOWN, STOPPED, PAUSED, SEEDING, SEEDWAIT, FINISHED, DOWNLOADING, QUEUED
	}
	
	String			name;
	Status			status;
	String			downloadedToDirectory;
	String			hashString;
	Double			percentDone;
	Date			activityDate;
	List<String>	files = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getDownloadedToDirectory() {
		return downloadedToDirectory;
	}
	public void setDownloadedToDirectory(String downloadedToDirectory) {
		this.downloadedToDirectory = downloadedToDirectory;
	}
	public String getHashString() {
		return hashString;
	}
	public void setHashString(String hashString) {
		this.hashString = hashString;
	}
	public Double getPercentDone() {
		return percentDone;
	}
	public void setPercentDone(Double percentDone) {
		this.percentDone = percentDone;
	}
	public Date getActivityDate() {
		return activityDate;
	}
	public void setActivityDate(Date activityDate) {
		this.activityDate = activityDate;
	}
	public List<String> getFiles() {
		return files;
	}
	public void setFiles(List<String> files) {
		this.files = files;
	}
	
}
