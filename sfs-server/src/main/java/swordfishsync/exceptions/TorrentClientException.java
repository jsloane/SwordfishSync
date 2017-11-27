package swordfishsync.exceptions;

public class TorrentClientException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public TorrentClientException(String message) {
		super(message);
	}
	
	public TorrentClientException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}
