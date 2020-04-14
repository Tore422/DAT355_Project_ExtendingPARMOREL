package hvl.projectparmorel.general;

import java.util.List;

public interface ErrorExtractor {
	/**
	 * Extracts the errors from the provided model.
	 * 
	 * @param model
	 * @param includeUnsupported is a boolean that specifies whether or not to include the errors supported by the {ModelFixer}.
	 * @return a list of errors found in the model. 
	 * @throws IllegalArgumentException if the model is not of a supported type
	 */
	public List<Error> extractErrorsFrom(Object model, boolean includeUnsupported);
}
