package hvl.projectparmorel.ml;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ContextMap implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * A map containing the actions for the given context.
	 */
	private Map<Integer, ActionMap> actions;
	
	public ContextMap() {
		actions = new HashMap<>();
	}
	
	public ActionMap getActionForContext(int contextId) {
		return actions.get(contextId);
	}
	
	public Set<Integer> getAlLContextIds(){
		return actions.keySet();
	}
}
