package swordfishsync.exceptions

class TorrentClientException extends Exception {
	
	public TorrentClientException(String message) {
		super(message);
	}
	
	public TorrentClientException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}
