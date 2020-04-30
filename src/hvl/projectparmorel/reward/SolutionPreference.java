package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.modelrepair.Solution;

interface SolutionPreference {
	
	/**
	 * Calculates reward for the solution with the resulting model. Each action can be rewarded through the Q-Table.
	 * 
	 * @param solution
	 * @param model
	 * @param qTable
	 * @return the 
	 */
	int rewardcalculateRewardFor(Solution solution, Model model, QTable qTable);
}
