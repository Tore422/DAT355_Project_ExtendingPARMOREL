package no.hvl.projectparmorel.exceptions;

/**
 * An exception stating no errors was found in the model.
 */
public class NoErrorsInModelException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	 * @param msg
	 */
	public NoErrorsInModelException(String msg) {
		super(msg);
	}
	
	public NoErrorsInModelException() {
		super();
	}
}
