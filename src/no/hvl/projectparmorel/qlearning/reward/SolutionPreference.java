package no.hvl.projectparmorel.qlearning.reward;

import no.hvl.projectparmorel.qlearning.Model;
import no.hvl.projectparmorel.qlearning.QSolution;
import no.hvl.projectparmorel.qlearning.knowledge.QTable;

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
