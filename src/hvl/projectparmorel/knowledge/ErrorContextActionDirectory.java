package hvl.projectparmorel.knowledge;

import java.util.List;
import java.util.Set;

public abstract class ErrorContextActionDirectory<T extends Comparable<T>> {
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
	 * Influence the weight of the scores by the once stored in prefereneScores if
	 * the preference is in preferences.
	 * 
	 * @param preferenceScores, the scores that should influence the QTable
	 * @param preferences, the preferences to be affected. Only preferences listed
	 *        where will be affected.
	 */
	public abstract void influenceWeightsByPreferedScores(ErrorContextActionDirectory<Action> preferenceScores,
			List<Integer> preferences);
	
	/**
	 * Gets the error map from the directory;
	 */
	protected abstract ErrorMap<T> getErrorMap();
	
	/**
	 * Gets the optimal context and action ID to handle the specified error.
	 * 
	 * @param errorCode
	 * @return the location of highest value in the context map. If two are equal,
	 *         one of them is returned. If the set is empty, null is returned.
	 */
	protected abstract T getOptimalActionForErrorCode(Integer errorCode);

	/**
	 * Checks that the provided value exists for the specified error code and context id
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the value exists for the specified error code and context ID, false otherwise.
	 */
	protected abstract boolean containsValueForErrorAndContext(int errorCode, int contextId, int actionId);
	
	/**
	 * Gets a random action for the specified error
	 * 
	 * @param errorCode
	 * @return a random action
	 */
	protected abstract T getRandomValueForError(int errorCode);
	
	/**
	 * Gets the action for the specified error code, context id and action id.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return the corresponding action
	 */
	protected abstract T getValue(Integer errorCode, Integer contextId, Integer actionId);
	
	/**
	 * Sets the value for the specified action in the specified context for the
	 * specified error. If the error, context or action is not in the hierarchy,
	 * they will be added.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param value
	 */
	public abstract void setValue(Integer errorCode, Integer contextId, Integer actionId, T value);
}