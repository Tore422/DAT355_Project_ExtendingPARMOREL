package hvl.projectparmorel.general;

import java.util.List;

public interface ActionExtractor {
	
	/**
	 * Extract all the actions that has the potential to solve the specified errors.
	 * 
	 * Optimally, only actions that results in a change to the model are included in the list to narrow the search space.
	 * 
	 * @param errors
	 * @return a list of actions
	 */
	public List<Action> extractActionsFor(List<Error> errors);
}
