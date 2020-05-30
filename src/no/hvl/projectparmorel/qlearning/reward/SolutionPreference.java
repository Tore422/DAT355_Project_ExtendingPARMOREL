package no.hvl.projectparmorel.qlearning.reward;

import no.hvl.projectparmorel.qlearning.Model;
import no.hvl.projectparmorel.qlearning.QSolution;
import no.hvl.projectparmorel.qlearning.knowledge.QTable;

interface SolutionPreference {

	/**
	 * Calculates reward for the solution with the resulting model. Each action
	 * should be rewarded through the
	 * {@link no.hvl.projectparmorel.qlearning.reward.Preference#rewardAction(QTable, int, int, int, int)
	 * rewardAction} method.
	 * 
	 * @param solution
	 * @param model
	 * @param qTable
	 * @return the reward.
	 */
	int rewardSolution(QSolution solution, Model model, QTable qTable);
}
