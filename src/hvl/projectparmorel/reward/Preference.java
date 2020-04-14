package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;

public abstract class Preference {
	protected int weight;
	protected PreferenceValue value;
	
	public Preference(int weight, PreferenceValue value) {
		this.weight = weight;
		this.value = value;
	}

	/**
	 * Calculates a reward for applying the specified action to the current error.
	 * 
	 * @param currentErrorToFix
	 * @param appliedAction
	 * @return the calculated reward
	 */
	public abstract int rewardActionForError(Error currentErrorToFix, Action appliedAction);
	
	/**
	 * Gets the weight specifying how much the preference is affecting the algorithm.
	 * 
	 * @return
	 */
	public int getWeight() {
		return weight;
	}
	
	/**
	 * Sets the weight specifying how much the preference should affect the algorithm.
	 * 
	 * @param weight
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	/**
	 * @return the value corresponding to the preference
	 */
	public PreferenceValue getPreferenceValue() {
		return value;
	}

	/**
	 * @param value the corresponding PreferenceValue for the preference to set
	 */
	public void setPreferenceValue(PreferenceValue value) {
		this.value = value;
	}
}
