package hvl.projectparmorel.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ErrorMap<T> {
	/**
	 * A map containing the context for the given error codes.
	 */
	private Map<Integer, ContextMap<T>> contexts;

	protected ErrorMap() {
		contexts = new HashMap<>();
	}

	/**
	 * Clears all the values, setting them to the provided value.
	 * 
	 * @param value to set
	 */
	protected void setAllValuesTo(T value) {
		for (ContextMap<T> context : contexts.values()) {
			context.setAllValuesTo(value);
		}
	}

	/**
	 * Gets all the error codes
	 * 
	 * @return a Set containing all the error codes.
	 */
	protected Set<Integer> getAllErrorCodes() {
		return contexts.keySet();
	}

	/**
	 * Influence the weight of the scores by the once stored in prefereneScores if
	 * the preference is in preferences.
	 * 
	 * @param preferenceScores, the ErrorMap that should influence the QTable
	 * @param preferences, the preferences to be affected. Only preferences listed
	 *        hwere will be affected.
	 */
	protected void influenceWeightsByPreferedScores(ErrorMap<Action> preferenceScores, List<Integer> preferences) {
		for (Integer errorCode : getAllErrorCodes()) {
			ContextMap<T> context = contexts.get(errorCode);
			context.influenceWeightsByPreferedScores(preferenceScores.getContextMapForErrorCode(errorCode),
					preferences);
		}
	}

	/**
	 * Returns the ContextMap for the given errorCode
	 * 
	 * @param errorCode
	 * @return the corresponding context map.
	 */
	private ContextMap<T> getContextMapForErrorCode(Integer errorCode) {
		return contexts.get(errorCode);
	}

	/**
	 * Checks that the provided error code is stored in the ErrorMap.
	 * 
	 * @param errorCode to check
	 * @return true if the errorCode is in the ErrorMap, false otherwise.
	 */
	protected boolean containsErrorCode(Integer errorCode) {
		return contexts.containsKey(errorCode);
	}
	
	/**
	 * Checks that the provided context id is stored in the Context Map for the given error.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @return true if the context ID exists in the map, false otherwise.
	 */
	protected boolean containsContextIdForErrorCode(Integer errorCode, Integer contextId) {
		return contexts.get(errorCode).containsContextId(contextId);
	}

	/**
	 * Checks that the provided action id is stored for the given error and context.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the action ID exists for the specified errorCode and contextId, false otherwise.
	 */
	protected boolean containsActionIdForErrorCodeAndContextId(int errorCode, int contextId, int actionId) {
		return contexts.get(errorCode).containsActionIdForContext(contextId, actionId);
	}
	
	/**
	 * Inserts a new entry in the map.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param weight
	 */
	protected void insertNewErrorCode(Integer errorCode, Integer contextId, Integer actionId, T value) {
		contexts.put(errorCode, new ContextMap<T>(contextId, actionId, value));
	}

	/**
	 * Inserts a new context for the specified error
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param value
	 */
	public void insertNewContext(Integer errorCode, Integer contextId, Integer actionId, T value) {
		ContextMap<T> contextForErrorCode = contexts.get(errorCode);
		contextForErrorCode.insertNewContext(contextId, actionId, value);	
	}

	/**
	 * Inserts a new action for the specified error code and context id.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param action
	 */
	protected void insertNewAction(int errorCode, int contextId, int actionId, T value) {
		ContextMap<T> contextForErrorCode = contexts.get(errorCode);
		contextForErrorCode.insertNewValueForContext(contextId, actionId, value);
	}
}
