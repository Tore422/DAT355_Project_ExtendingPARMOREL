package no.hvl.projectparmorel.qlearning.reward;

import java.util.List;

import no.hvl.projectparmorel.qlearning.QSolution;
import no.hvl.projectparmorel.qlearning.knowledge.QTable;

interface PostRepairPreference {

	/**
	 * Calculates rewards that are calculated after the repair process is finished.
	 * This is added to the knowledge directly, so it can affect the next repair
	 * process.
	 * 
	 * @param possibleSolutions
	 * @param qTable
	 */
	void rewardPostRepair(List<QSolution> possibleSolutions, QTable qTable);
}
