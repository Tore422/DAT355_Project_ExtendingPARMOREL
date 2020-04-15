package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;

public interface ActionBasedPreference {
	/**
	 * Calculates a reward for applying the specified action to the current error.
	 * 
	 * @param currentErrorToFix
	 * @param appliedAction
	 * @return the calculated reward
	 */
	int rewardActionForError(Error currentErrorToFix, Action appliedAction);
}
