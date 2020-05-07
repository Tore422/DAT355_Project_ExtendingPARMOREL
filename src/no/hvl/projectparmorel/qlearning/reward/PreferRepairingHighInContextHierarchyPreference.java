package no.hvl.projectparmorel.qlearning.reward;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.Model;

public class PreferRepairingHighInContextHierarchyPreference extends Preference {
	
	public PreferRepairingHighInContextHierarchyPreference(int weight) {
		super(weight, PreferenceOption.REPAIR_HIGH_IN_CONTEXT_HIERARCHY);
	}
	
	@Override
	public int rewardActionForError(Model model, Error error, Action action) {
		int reward = 0;
		
		if (action.getContextId() == 1) {
			reward += weight;
		} else if (action.getContextId() == 2) {
			reward += weight * 2 / 3;
		} else if (action.getContextId() > 2) {
			reward -= -74 / 100 * weight;
		}
		return reward;
	}
}
