package hvl.projectparmorel.reward;

import java.util.List;

import hvl.projectparmorel.general.Solution;
import hvl.projectparmorel.knowledge.QTable;

interface PostRepairPreference {

	/**
	 * Calculates rewards that are calculated after the repair process is finished.
	 * This is added to the knowledge directly, so it can affect the next repair
	 * process.
	 * 
	 * @param possibleSolutions
	 * @param qTable
	 */
	void rewardPostRepair(List<Solution> possibleSolutions, QTable qTable);
}
