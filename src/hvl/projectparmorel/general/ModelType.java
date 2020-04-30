package hvl.projectparmorel.general;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum ModelType {
	ECORE(new HashSet<Integer>(Arrays.asList(1, 4)));

	private Set<Integer> unsupportedErrorCodes;

	ModelType(Set<Integer> unsupportedErrorCodes) {
		this.unsupportedErrorCodes = unsupportedErrorCodes;
	}

	/**
	 * Add an unsupported error code to the registry
	 * 
	 * @param errorCode
	 */
	public void addUnsupportedErrorCode(Integer errorCode) {
		unsupportedErrorCodes.add(errorCode);
	}

	/**
	 * Get the unsupported error codes
	 * 
	 * @return the unsupported error codes
	 */
	public Set<Integer> getUnsupportedErrorCodes() {
		return unsupportedErrorCodes;
	}

	/**
	 * Checks if the model type has discovered it does not support an error yet. A
	 * false response might also mean the algorithm has not yet discovered that it
	 * does not support the error code.
	 * 
	 * @param errorCode
	 * @return true if the set of unsupported errors contains the error code, false
	 *         otherwise.
	 */
	public boolean doesNotSupportError(Integer errorCode) {
		return unsupportedErrorCodes.contains(errorCode);
	}
}
