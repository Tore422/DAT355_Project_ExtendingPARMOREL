package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;

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
