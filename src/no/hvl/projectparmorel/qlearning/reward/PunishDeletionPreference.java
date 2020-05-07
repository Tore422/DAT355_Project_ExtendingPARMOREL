package no.hvl.projectparmorel.qlearning.reward;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.Model;

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
