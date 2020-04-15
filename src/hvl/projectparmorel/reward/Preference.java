package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;

abstract class Preference {
	protected int weight;
	protected PreferenceValue value;
	
	protected Preference(int weight, PreferenceValue value) {
		this.weight = weight;
		this.value = value;
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
	PreferenceValue getPreferenceValue() {
		return value;
	}

	/**
	 * @param value the corresponding PreferenceValue for the preference to set
	 */
	void setPreferenceValue(PreferenceValue value) {
		this.value = value;
	}
}
