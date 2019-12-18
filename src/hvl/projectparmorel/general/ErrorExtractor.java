package hvl.projectparmorel.general;

import java.util.List;

public interface ErrorExtractor {
	/**
	 * Extracts the errors from the provided model.
	 * 
	 * @param model
	 * @return a list of errors found in the model. The list should not include errors that are not supported by the {ModelFixer}.
	 * @throws IllegalArgumentException if the model is not of a supported type
	 */
	public List<Error> extractErrorsFrom(Object model);
}
