package hvl.projectparmorel.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ActionMap<T> {
	/**
	 * A map containing the actions for the given context.
	 */
	private Map<Integer, T> actions;

	protected ActionMap() {
		actions = new HashMap<>();
	}

	protected ActionMap(Integer actionId, T value) {
		actions = new HashMap<>();
		actions.put(actionId, value);
	}

	/**
	 * Clears all the values, setting them to the provided value.
	 * 
	 * @param value to set
	 */
	protected void setAllValuesTo(T value) {
		for (Integer actionKey : actions.keySet()) {
			actions.put(actionKey, value);
		}
	}

	/**
	 * Influences the weights in the QTable from the action map if the action is in the preferences.
	 * 
	 * @param actionMapForContext
	 * @param preferences
	 */
	@SuppressWarnings("unchecked")
	protected void influenceWeightsByPreferedScores(ActionMap<Action> actionMapForContext, List<Integer> preferences) {
		for (Integer actionId : actions.keySet()) {
			Action action = actionMapForContext.getElementForActionId(actionId);
			TagDictionary tagDictionary = action.getTagDictionary();
			for (Integer tagId : tagDictionary.getAllTagIds()) {
				if (preferences.contains(tagId)) {
					Double value = tagDictionary.getTagFor(tagId) * 0.2;					
					value += (double) actions.get(actionId);
					
					if(actions.values().toArray()[0] instanceof Double) {
						actions.put(actionId, (T) value);
					} else {
						throw new IllegalStateException("The QTable must be parametrized with Double.");
					}
					
				}
			}
		}
	}

	/**
	 * Get element for the corresponding action Id.
	 * 
	 * @param actionId, the id for the action
	 * @return the corresponding element
	 */
	private T getElementForActionId(Integer actionId) {
		return actions.get(actionId);
	}

	/**
	 * Checks that the action map contains a given action id.
	 * 
	 * @param actionId
	 * @return true if the action ID exists, false otherwise.
	 */
	protected boolean containsAction(int actionId) {
		return actions.containsKey(actionId);
	}

	/**
	 * Adds a new action to the action map.
	 * 
	 * @param actionId
	 * @param value
	 */
	protected void addValue(int actionId, T value) {
		actions.put(actionId, value);
	}
}
