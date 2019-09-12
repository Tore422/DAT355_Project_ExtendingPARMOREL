package hvl.projectparmorel.reward;

import hvl.projectparmorel.ml.Action;
import hvl.projectparmorel.ml.QLearning;

public class RewardCalculator {
	
	private int weightPunishDeletion;
	
	public RewardCalculator(int weightPunishDeletion) {
		this.weightPunishDeletion = weightPunishDeletion;
	}

	/**
	 * Initializes the weight for the given action
	 * 
	 * @param action
	 * @return initial weight
	 */
	public double initializeWeightFor(Action action) {
		double weight = 0.0;

		if (QLearning.preferences.contains(4)) {
			if (action.getMsg().contains("delete")) {
				weight = -(double) weightPunishDeletion / 100;
			} else {
				weight = 0.0;
			}
		}

		if (action.getMsg().contains("get")) {
			weight = -10.0;
		} else {
			weight = 0.0;
		}

		return weight;
	}
}
