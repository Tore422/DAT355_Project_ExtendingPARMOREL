package hvl.projectparmorel.knowledge;

import java.util.List;
import java.util.Set;

abstract class ErrorContextActionDirectory<T> {
	public ErrorContextActionDirectory() {
		
	}
	
	/**
	 * Sets all the values to the provided value.
	 * 
	 * @param value to set
	 */
	public abstract void setAllValuesTo(T value);
	
	/**
	 * Gets all the error codes
	 * 
	 * @return a Set containing all the error codes.
	 */
	public abstract Set<Integer> getAllErrorCodes();

	/**
	 * Influence the weight of the scores by the once stored in prefereneScores if the preference is in preferences.
	 * 
	 * @param preferenceScores, the scores that should influence the QTable
	 * @param preferences, the preferences to be affected. Only preferences listed hwere will be affected.
	 */
	public abstract void influenceWeightsByPreferedScores(ErrorContextActionDirectory<Action> preferenceScores,
			List<Integer> preferences);
	
	/**
	 * Gets the error map from the directory;
	 */
	protected abstract ErrorMap<T> getErrorMap();

}

