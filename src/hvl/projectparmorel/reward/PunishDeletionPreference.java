package hvl.projectparmorel.reward;

import hvl.projectparmorel.qlearning.Action;
import hvl.projectparmorel.qlearning.Error;
import hvl.projectparmorel.qlearning.Model;

public class PunishDeletionPreference extends Preference {

	public PunishDeletionPreference(int weight) {
		super(weight, PreferenceOption.PUNISH_DELETION);
	}
	
	@Override
	public int rewardActionForError(Model model, Error error, Action action) {
		int reward = 0;
		if (action.isDelete()) {
			reward -= weight;
		} else {
			reward += weight / 10;
		}
		return reward;
	}

}
