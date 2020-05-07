package no.hvl.projectparmorel.qlearning;

import java.util.List;

import no.hvl.projectparmorel.qlearning.knowledge.QTable;

public interface ActionExtractor {
	
	/**
	 * Extract all the actions that has the potential to solve the specified errors that is not already in the Q-table.
	 * 
	 * Optimally, only actions that results in a change to the model are included in the list to narrow the search space.
	 * 
	 * @param qTable - the Q-Table to check if action for errors exist
	 * @param errors - the errors the actions should fix
	 * @return a list of actions
	 */
	public List<Action> extractActionsNotInQTableFor(QTable qTable, List<Error> errors);
}
