package hvl.projectparmorel.ml;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActionMap implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * A map containing the actions for the given context.
	 */
	private Map<Integer, ActionExp> actions;
	
	public ActionMap() {
		actions = new HashMap<>();
	}
	
	/**
	 * Gets all the action IDs
	 * 
	 * @return all the action IDs
	 */
	public Set<Integer> getAllActionIds(){
		return actions.keySet();
	}
	
	public ActionExp getAction(Integer id) {
		return actions.get(id);
	}
}
