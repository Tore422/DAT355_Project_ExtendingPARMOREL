package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;

public class PunishDeletionPreference extends Preference {

	public PunishDeletionPreference(int weight) {
		super(weight, PreferenceValue.PUNISH_DELETION);
	}
	
	@Override
	public int rewardActionForError(Model model, Error error, Action action) {
		int reward = 0;
//		int contextId = action.getHierarchy();
		if (action.isDelete()) {
			reward -= weight;
//			addTagMap(currentErrorToFix, contextId, action, 4, -weight);
		} else {
			reward += weight / 10;
//			addTagMap(currentErrorToFix, contextId, action, 4, weight / 10);
		}
		return reward;
	}

}
