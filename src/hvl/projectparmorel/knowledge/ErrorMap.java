package hvl.projectparmorel.knowledge;

import java.util.HashMap;
import java.util.Map;

class ErrorMap<T> {
	/**
	 * A map containing the context for the given error codes.
	 */
	private Map<Integer, ContextMap<T>> contexts;
	
	protected ErrorMap() {
		contexts = new HashMap<>();
	}
	
	/**
	 * Clears all the values, setting them to the provided default value.
	 */
	protected void setAllValuesTo(T defaultValue) {
		for(ContextMap<T> context : contexts.values()) {
			context.setAllValuesTo(defaultValue);
		}

	}	
}
