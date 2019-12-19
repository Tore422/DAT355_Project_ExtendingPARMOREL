package hvl.projectparmorel.general;

import java.util.List;

public interface ModelProcessor {
	
	/**
	 * Goes through all the errors in the model, and if the error is not in the
	 * q-table is is added along with a matching action.
	 * 
	 * @param model
	 */
	public void initializeQTableForErrorsInModel(Model model);

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
