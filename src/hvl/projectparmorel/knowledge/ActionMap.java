package hvl.projectparmorel.knowledge;

import java.util.HashMap;
import java.util.Map;

class ActionMap<T> {
	/**
	 * A map containing the actions for the given context.
	 */
	private Map<Integer, T> actions;
	
	protected ActionMap() {
		actions = new HashMap<>();
	}
	
	/**
	 * Clears all the values, setting them to the provided default value.
	 */
	protected void setAllValuesTo(T defaultValue) {
		for(Integer actionKey : actions.keySet()) {
			actions.put(actionKey, defaultValue);
		}
	}
}
