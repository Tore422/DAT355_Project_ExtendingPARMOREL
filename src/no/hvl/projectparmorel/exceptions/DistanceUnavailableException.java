package no.hvl.projectparmorel.exceptions;

/**
 * An exception specifying that the distance is unavailable, either because it
 * could not be calculated or because it is not implemented.
 * 
 * @author Magnus
 */
public class DistanceUnavailableException extends Exception {

	private static final long serialVersionUID = 1L;

	public DistanceUnavailableException() {
		super();
	}
	
	public DistanceUnavailableException(String message) {
		super(message);
	}
	
	public DistanceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
