package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;

public class PreferRepairingHighInContextHierarchyPreference extends Preference {
	
	public PreferRepairingHighInContextHierarchyPreference(int weight) {
		super(weight, PreferenceValue.REPAIR_HIGH_IN_CONTEXT_HIERARCHY);
	}
	
	@Override
	public int rewardActionForError(Model model, Error error, Action action) {
		int reward = 0;
		
//		int contextId = action.getHierarchy();
		if (action.getHierarchy() == 1) {
			reward += weight;
//			addTagMap(currentErrorToFix, contextId, action, 2, weight);
		} else if (action.getHierarchy() == 2) {
			reward += weight * 2 / 3;
//			addTagMap(currentErrorToFix, contextId, action, 2, weight * 2 / 3);
		} else if (action.getHierarchy() > 2) {
			reward -= -74 / 100 * weight;
//			addTagMap(currentErrorToFix, contextId, action, 2, -74 / 100 * weight);
		}
		return reward;
	}
}
