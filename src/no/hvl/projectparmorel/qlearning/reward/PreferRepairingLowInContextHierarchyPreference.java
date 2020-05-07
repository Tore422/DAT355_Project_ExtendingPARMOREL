package no.hvl.projectparmorel.qlearning.reward;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.Model;

public class PreferRepairingLowInContextHierarchyPreference extends Preference {

	public PreferRepairingLowInContextHierarchyPreference(int weight) {
		super(weight, PreferenceOption.REPAIR_LOW_IN_CONTEXT_HIERARCHY);
	}

	@Override
	public int rewardActionForError(Model model, Error error, Action action) {
		int reward = 0;
		if (action.getContextId() == 1) {
			reward -= 74 / 100 * weight;
		}
		if (action.getContextId() == 2) {
			reward += weight * 2 / 3;
		}
		if (action.getContextId() > 2) {
			reward += weight;
		}
		return reward;
	}
}
