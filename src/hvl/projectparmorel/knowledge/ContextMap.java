package hvl.projectparmorel.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ContextMap<T> {
	/**
	 * A map containing the actions for the given context.
	 */
	private Map<Integer, ActionMap<T>> actions;
	
	protected ContextMap() {
		actions = new HashMap<>();
	}
	
	protected ContextMap(Integer contextId, Integer actionId, T value) {
		actions = new HashMap<>();
		actions.put(contextId, new ActionMap<T>(actionId, value));
	}

	/**
	 * Clears all the values, setting them to the provided value.
	 * 
	 * @param value to set
	 */
	protected void setAllValuesTo(T value) {
		for (ActionMap<T> action : actions.values()) {
			action.setAllValuesTo(value);
		}
	}

	/**
	 * Influence the weight of the scores by the once stored in prefereneScores if the preference is in preferences.
	 * 
	 * @param contextMapForErrorCode
	 * @param preferences
	 */
	protected void influenceWeightsByPreferedScores(ContextMap<Action> contextMapForErrorCode, List<Integer> preferences) {
		for(Integer contextId : actions.keySet()) {
			ActionMap<T> actionMapForContext = actions.get(contextId);
			actionMapForContext.influenceWeightsByPreferedScores(contextMapForErrorCode.getActionMapForContext(contextId), preferences);
		}
	}
	
	/**
	 * Gets the action map for the specified context
	 * 
	 * @param contextId, the contextId to get the actionMap for.
	 * @return the corresponding actionMap
	 */
	private ActionMap<T> getActionMapForContext(Integer contextId){
		return actions.get(contextId);
	}

	/**
	 * Checks that the provided context id is stored in the Context Map.
	 * 
	 * @param contextId
	 * @return true if the context ID exists in the map, false otherwise.
	 */
	protected boolean containsContextId(Integer contextId) {
		return actions.containsKey(contextId);
	}

	/**
	 * Inserts a new context for the given error code.
	 * 
	 * @param contextId
	 * @param actionId
	 * @param value
	 */
	protected void insertNewContext(Integer contextId, Integer actionId, T value) {
		if(actions.containsKey(contextId)) {
			throw new IllegalStateException("The context ID allready exists for the given error code.");
		}
		actions.put(contextId, new ActionMap<T>(actionId, value));
		
	}

	/**
	 * Checks that the provided action ID exists for the given context
	 * 
	 * @param contextId
	 * @param actionId
	 * @return true if the action ID exists for the given context, false otherwise.
	 */
	protected boolean containsActionIdForContext(int contextId, int actionId) {
		return actions.get(contextId).containsAction(actionId);
	}

	/**
	 * Inserts a new value for the specified context id.
	 * 
	 * @param contextId
	 * @param value
	 */
	protected void insertNewValueForContext(int contextId, int actionId, T value) {
		ActionMap<T> actionMapForContext = actions.get(contextId);
		actionMapForContext.addValue(actionId, value);
		
	}
}
