package hvl.projectparmorel.knowledge;

import java.util.HashMap;
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
	 * Clears all the values, setting them to the provided default value.
	 */
	protected void setAllValuesTo(T defaultValue) {
		for (ActionMap<T> action : actions.values()) {
			action.setAllValuesTo(defaultValue);
		}
	}
}
