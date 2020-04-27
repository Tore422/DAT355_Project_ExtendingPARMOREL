package hvl.projectparmorel.general;

import java.util.List;

/**
 * 
 * @author Magnus Marthinsen
 * @author Angela Barriga Rodriguez - 2019 abar@hvl.no Western Norway University
 *         of Applied Sciences Bergen - Norway
 */
public class Error {

	private int code;
	private String message;
	private List<?> contexts;
	private int packageIndex;

	public Error() {
	}

	public Error(int code, String message, List<?> contexts,  int packageIndex) {
		this.code = code;
		this.message = message;
		this.contexts = contexts;
		this.packageIndex = packageIndex;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Error) {
			Error otherError = (Error) other;
			return code == otherError.getCode() && message.equals(otherError.getMessage())
					&& contexts.equals(otherError.getContexts())
					&& packageIndex == otherError.getPackageIndex();
		}
		return false;
	}

	@Override
	public String toString() {
		return "Error " + code + ", message=" + message + " in package " + packageIndex + System.getProperty("line.separator");

	}

	/**
	 * Gets the error code unique to a specific error type.
	 * 
	 * @return the error code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Sets the error code unique to a specific error type.
	 * 
	 * @param the error code
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * Gets the message explaining the error
	 * 
	 * @return a message explaining the error
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message explaining the error
	 * 
	 * @param message explaining the error
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Gets a list of contexts from which the error could be repaired, i.e. objects in the model related to the error.
	 * 
	 * @return a list of contexts
	 */
	public List<?> getContexts() {
		return contexts;
	}

	/**
	 * Sets a list of contexts from which the error could be repaired, i.e. objects in the model related to the error.
	 * 
	 * @return a list of contexts
	 */
	public void setContexts(List<?> contexts) {
		this.contexts = contexts;
	}

	/**
	 * Gets the index of the package where the error resides.
	 * 
	 * @return the index where the error resides
	 */
	public int getPackageIndex() {
		return packageIndex;
	}

	/**
	 * Sets the index of the package where the error resides.
	 * 
	 * @param packageIndex
	 */
	public void setPackageIndex(int packageIndex) {
		this.packageIndex = packageIndex;
	}
}
