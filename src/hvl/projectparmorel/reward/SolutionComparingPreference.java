package hvl.projectparmorel.reward;

import java.util.List;

import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.modelrepair.Solution;

interface SolutionComparingPreference {
	
	/**
	 * Calculates rewards that compare the different solutions to each other.
	 * 
	 * @param possibleSolutions
	 * @param knowledge 
	 */
	void rewardPostRepair(List<Solution> possibleSolutions, Knowledge knowledge);
}
