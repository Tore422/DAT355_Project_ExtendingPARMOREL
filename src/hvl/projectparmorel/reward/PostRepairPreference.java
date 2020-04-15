package hvl.projectparmorel.reward;

import java.util.List;

import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.modelrepair.Solution;

interface PostRepairPreference {

	/**
	 * Calculates rewards that are calculated after the repair process is finished.
	 * This is added to the knowledge directly, so it can affect the next repair
	 * process.
	 * 
	 * @param possibleSolutions
	 * @param knowledge
	 */
	void rewardPostRepair(List<Solution> possibleSolutions, Knowledge knowledge);
}
