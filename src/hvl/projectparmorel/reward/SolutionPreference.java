package hvl.projectparmorel.reward;

import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.qlearning.Model;
import hvl.projectparmorel.qlearning.QSolution;

interface SolutionPreference {
	
	/**
	 * Calculates reward for the solution with the resulting model. Each action can be rewarded through the Q-Table.
	 * 
	 * @param solution
	 * @param model
	 * @param qTable
	 * @return the 
	 */
	int rewardcalculateRewardFor(QSolution solution, Model model, QTable qTable);
}
