package hvl.projectparmorel.exceptions;

public class UnsupportedErrorException extends Exception{
	
	private int errorCode;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnsupportedErrorException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}
	
	public UnsupportedErrorException(String msg, int errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
