package swordfishsync.exceptions;

public class ApplicationException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public ApplicationException(String message) {
		super(message);
	}
	
	public ApplicationException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}
