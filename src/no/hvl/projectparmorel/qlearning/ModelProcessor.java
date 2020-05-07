package no.hvl.projectparmorel.qlearning;

import java.util.List;
import java.util.Set;

public interface ModelProcessor {
	
	/**
	 * Goes through all the errors in the model, and if the error is not in the
	 * q-table is is added along with matching actions. Alternatively the error code
	 * is added to a set of unsupported errors.
	 * 
	 * @param model
	 * @return a set of unsupported error codes that was not added to the Q-table
	 */
	public Set<Integer> initializeQTableForErrorsInModel(Model model);

	/**
	 * Extracts package content from the model, and matches the location where the
	 * error resides to the correct type and tries to apply the action to this error
	 * location.
	 * 
	 * @param error
	 * @param action
	 * @param model
	 * @return a list of new errors if the action was successfully applied, null
	 *         otherwise
	 */
	public List<Error> tryApplyAction(Error error, Action action, Model model);
}
