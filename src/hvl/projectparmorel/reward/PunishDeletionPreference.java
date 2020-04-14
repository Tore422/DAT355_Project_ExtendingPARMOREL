package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;

public class PunishDeletionPreference extends Preference {

	public PunishDeletionPreference(double weight) {
		super(weight);
	}
	
	@Override
	public double rewardActionForError(Error currentErrorToFix, Action action) {
		double reward = 0;
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
