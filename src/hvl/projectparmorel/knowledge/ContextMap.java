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
}
