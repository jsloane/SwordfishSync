package mymedia.exceptions;

public class ApplicationException extends Exception {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 8791709682752813948L;
	
	public ApplicationException(String message) {
        super(message);
    }
	
    public ApplicationException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
}