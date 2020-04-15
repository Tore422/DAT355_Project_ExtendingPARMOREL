package hvl.projectparmorel.general;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum ModelType {
	ECORE(new HashSet<Integer>(Arrays.asList(1, 4))); //6
	
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
}
