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
	 * Influence the weight of the scores by the once stored in prefereneScores if the preference is in preferences.
	 * 
	 * @param preferenceScores, the scores that should influence the QTable
	 * @param preferences, the preferences to be affected. Only preferences listed where will be affected.
	 */
	public abstract void influenceWeightsByPreferedScores(ErrorContextActionDirectory<Action> preferenceScores,
			List<Integer> preferences);
	
	/**
	 * Inserts a new entry in the directory.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param weight
	 */
	public abstract void insertNewErrorCode(Integer errorCode, Integer contextId, Integer actionId, T value);
	
	/**
	 * Inserts a new context within existing error code.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param value
	 */
	protected abstract void addContextToError(Integer errorCode, Integer contextId, Integer actionId, T value);
	
	/**
	 * Gets the error map from the directory;
	 */
	protected abstract ErrorMap<T> getErrorMap();

	/**
	 * Adds an action for the specified error code and context id
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param action
	 */
	protected abstract void addValue(int errorCode, int contextId, int actionId, T value);

	/**
	 * Updates the weight for the action specified by the error code, context id and action id.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param weight
	 */
	protected abstract void updateValue(Integer errorCode, Integer contextId, Integer actionId, T value);

	/**
	 * Gets the optimal context and action ID to handle the specified error.
	 * 
	 * @param errorCode
	 * @return the location of highest value in the context map. If two are equal, one of them is returned. If the set is empty, null is returned.
	 */
	protected abstract ActionLocation getOptimalActionIndexForErrorCode(Integer errorCode);

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
	 * Gets a random action for the specified error
	 * 
	 * @param errorCode
	 * @return a random action
	 */
	protected abstract T getRandomValueForError(int errorCode);
}