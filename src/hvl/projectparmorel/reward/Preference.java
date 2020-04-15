package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;

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
	 * @param model after applying action
	 * @param error that is being fixed
	 * @param action applied to try and fix the error
	 * @return the calculated reward
	 */
	abstract int rewardActionForError(Model model, Error error, Action action);
	
	/**
	 * Gets the weight specifying how much the preference is affecting the algorithm.
	 * 
	 * @return
	 */
	int getWeight() {
		return weight;
	}
	
	/**
	 * Sets the weight specifying how much the preference should affect the algorithm.
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
