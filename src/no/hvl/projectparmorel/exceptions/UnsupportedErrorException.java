package no.hvl.projectparmorel.exceptions;

/**
 * Error caused by an error code that is not supported.
 * 
 * @author Magnus
 */
public class UnsupportedErrorException extends Exception{
	
	private int errorCode;

	private static final long serialVersionUID = 1L;

	/**
	 * @param errorCode that caused the exception
	 */
	public UnsupportedErrorException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}
	
	/**
	 * @param msg
	 * @param errorCode that caused the exception
	 */
	public UnsupportedErrorException(String msg, int errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}
	
	/**
	 * Get the error code that caused the exception
	 * 
	 * @return the error code that caused the exception
	 */
	public int getErrorCode() {
		return errorCode;
	}
}
