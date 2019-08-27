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
		for(ContextMap<T> context : contexts.values()) {
			context.setAllValuesTo(value);
		}
	}	
	
	/**
	 * Gets all the error codes
	 * 
	 * @return a Set containing all the error codes.
	 */
	protected Set<Integer> getAllErrorCodes(){
		return contexts.keySet();
	}

	/**
	 * Influence the weight of the scores by the once stored in prefereneScores if the preference is in preferences.
	 * 
	 * @param preferenceScores, the ErrorMap that should influence the QTable
	 * @param preferences, the preferences to be affected. Only preferences listed hwere will be affected.
	 */
	protected void influenceWeightsByPreferedScores(ErrorMap<Action> preferenceScores,
			List<Integer> preferences) {
		for(Integer errorCode : getAllErrorCodes()) {
			ContextMap<T> context = contexts.get(errorCode);
			context.influenceWeightsByPreferedScores(preferenceScores.getContextMapForErrorCode(errorCode), preferences);
		}
	}
	
	/**
	 * Returns the ContextMap for the given errorCode
	 * 
	 * @param errorCode
	 * @return the corresponding context map.
	 */
	private ContextMap<T> getContextMapForErrorCode(Integer errorCode){
		return contexts.get(errorCode);
	}
}
