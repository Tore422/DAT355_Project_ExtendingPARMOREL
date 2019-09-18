package hvl.projectparmorel.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

class ActionMap<T extends Comparable<T>> {
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
	 * Influences the weights in the QTable from the action map if the action is in
	 * the preferences.
	 * 
	 * @param actionMapForContext
	 * @param preferences
	 */
	@SuppressWarnings("unchecked")
	protected void influenceWeightsByPreferedScores(ActionMap<Action> actionMapForContext, List<Integer> preferences) {
		for (Integer actionId : actions.keySet()) {
			Action action = actionMapForContext.getValue(actionId);
			PreferenceWeightMap tagDictionary = action.getTagDictionary();
			for (Integer tagId : tagDictionary.getAllPreferenceIds()) {
				if (preferences.contains(tagId)) {
					Double value = tagDictionary.getWeightFor(tagId) * 0.2;
					value += (double) actions.get(actionId);

					if (actions.values().toArray()[0] instanceof Double) {
						actions.put(actionId, (T) value);
					} else {
						throw new IllegalStateException("The QTable must be parametrized with Double.");
					}

				}
			}
		}
	}

	/**
	 * Checks that the action map contains a given action id.
	 * 
	 * @param actionId
	 * @return true if the action ID exists, false otherwise.
	 */
	protected boolean containsValue(int actionId) {
		return actions.containsKey(actionId);
	}

	/**
	 * Gets the key for highest value
	 * 
	 * @return the highest value in the action map. If two are equal, one of them is
	 *         returned. If the set is empty, null is returned.
	 */
	protected Integer getHihgestValueKey() {
		Set<Integer> actionIdSet = actions.keySet();
		Integer[] actionIds = new Integer[actionIdSet.size()];
		actionIds = actionIdSet.toArray(actionIds);
		if (actionIds.length > 0) {
			Integer optimalActionId = actionIds[0];

			for (int i = 1; i < actionIds.length; i++) {
				T optimalAction = actions.get(optimalActionId);
				T action = actions.get(actionIds[i]);
				if (action.compareTo(optimalAction) > 0) {
					optimalActionId = actionIds[i];
				}
			}
			return optimalActionId;
		}
		return null;
	}

	/**
	 * Gets a random value
	 * 
	 * @return a random value
	 */
	protected T getRandomValue() {
		Random randomGenerator = new Random();
		Integer[] actionIds = new Integer[actions.keySet().size()];
		actionIds = actions.keySet().toArray(actionIds);
		int randomActionIndex = randomGenerator.nextInt(actionIds.length);
		return actions.get(actionIds[randomActionIndex]);
	}
	
	/**
	 * Sets the value for the specified action. If the action is not in the
	 * hierarchy, it will be added.
	 * 
	 * @param actionId
	 * @param value
	 */
	protected void setValue(Integer actionId, T value) {
		actions.put(actionId, value);
	}
	
	/**
	 * Gets the value for the specified action
	 * 
	 * @param actionId
	 * @return the value for the specified action
	 */
	protected T getValue(Integer actionId) {
		return actions.get(actionId);
	}
}
