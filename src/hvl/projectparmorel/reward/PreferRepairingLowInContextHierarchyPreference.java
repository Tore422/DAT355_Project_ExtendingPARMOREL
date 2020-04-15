package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;

public class PreferRepairingLowInContextHierarchyPreference extends Preference {

	public PreferRepairingLowInContextHierarchyPreference(int weight) {
		super(weight, PreferenceValue.REPAIR_LOW_IN_CONTEXT_HIERARCHY);
	}

	@Override
	public int rewardActionForError(Model model, Error error, Action action) {
		int reward = 0;
//		int contextId = action.getHierarchy();
		if (action.getHierarchy() == 1) {
			reward -= 74 / 100 * weight;
//			addTagMap(currentErrorToFix, contextId, action, 3, -74 / 100 * weight);
		}
		if (action.getHierarchy() == 2) {
			reward += weight * 2 / 3;
//			addTagMap(currentErrorToFix, contextId, action, 3, weight * 2 / 3);
		}
		if (action.getHierarchy() > 2) {
			reward += weight;
//			addTagMap(currentErrorToFix, contextId, action, 3, weight);
		}
		return reward;
	}
}
