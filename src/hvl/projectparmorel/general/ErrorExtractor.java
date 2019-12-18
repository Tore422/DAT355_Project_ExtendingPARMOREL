package hvl.projectparmorel.general;

import java.util.List;

public interface ErrorExtractor {
	/**
	 * Extracts the errors from the provided model.
	 * 
	 * @param model
	 * @return a list of errors found in the model
	 */
	public List<Error> extractErrorsFrom(Object model);
}
