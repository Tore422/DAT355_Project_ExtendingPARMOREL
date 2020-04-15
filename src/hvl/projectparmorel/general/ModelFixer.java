package hvl.projectparmorel.general;

import java.io.File;
import java.util.List;

import hvl.projectparmorel.exceptions.NoErrorsInModelException;
import hvl.projectparmorel.modelrepair.Solution;
import hvl.projectparmorel.reward.PreferenceOption;

public interface ModelFixer {

	/**
	 * Fixes the model provided as attribute, and stores the repaired model in the
	 * uri-location.
	 * 
	 * @param model
	 * @throws NoErrorsInModelException if there are no errors in the model.
	 * @return the optimal sequence of actions
	 */
	public Solution fixModel(File model) throws NoErrorsInModelException ;

	/**
	 * Sets the user preferences used in the algorithm.
	 * 
	 * @param preferences
	 */
	public void setPreferences(List<PreferenceOption> preferences);

	/**
	 * Gets the list of possible solutions. Requires that
	 * {@link ModelFixer#fixModel}-method has been called.
	 * 
	 * @return a list of possible solutions.
	 */
	public List<Solution> getPossibleSolutions();
}
