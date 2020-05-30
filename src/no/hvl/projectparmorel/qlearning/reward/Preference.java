package no.hvl.projectparmorel.qlearning.reward;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.Model;
import no.hvl.projectparmorel.qlearning.QModelFixer;
import no.hvl.projectparmorel.qlearning.knowledge.QTable;

abstract class Preference {
	protected int weight;
	protected PreferenceOption preferenceOption;

	protected Preference(int weight, PreferenceOption preferenceOption) {
		this.weight = weight;
		this.preferenceOption = preferenceOption;
	}

	/**
	 * Calculates a reward for applying the specified action to the current error.
	 * 
	 * @param model  after applying action
	 * @param error  that is being fixed
	 * @param action applied to try and fix the error
	 * @return the calculated reward
	 */
	abstract int rewardActionForError(Model model, Error error, Action action);

	/**
	 * Rewards the provided action in the Q-table specified by the parameters with
	 * the specified reward. The reward is not set directly, but passed through the
	 * {@link no.hvl.projectparmorel.qlearning.QModelFixer#updateQTable(QTable, int, int, int, int)
	 * updateQTable} method.
	 * 
	 * @param qTable
	 * @param errorId
	 * @param contextId
	 * @param actionId
	 * @param reward
	 */
	protected void rewardAction(QTable qTable, int errorId, int contextId, int actionId, int reward) {
		QModelFixer.updateQTable(qTable, errorId, contextId, actionId, reward);
	}

	/**
	 * Gets the weight specifying how much the preference is affecting the
	 * algorithm.
	 * 
	 * @return
	 */
	int getWeight() {
		return weight;
	}

	/**
	 * Sets the weight specifying how much the preference should affect the
	 * algorithm.
	 * 
	 * @param weight
	 */
	void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * @return the value corresponding to the preference
	 */
	PreferenceOption getPreferenceOption() {
		return preferenceOption;
	}

	/**
	 * @param option the corresponding option for the preference to set
	 */
	void setPreferenceOption(PreferenceOption option) {
		this.preferenceOption = option;
	}
}
