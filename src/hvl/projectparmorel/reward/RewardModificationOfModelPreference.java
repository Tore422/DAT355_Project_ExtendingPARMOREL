package hvl.projectparmorel.reward;

import hvl.projectparmorel.ecore.EcoreErrorExtractor;
import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.ErrorExtractor;
import hvl.projectparmorel.general.Model;

public class RewardModificationOfModelPreference extends Preference implements ResultBasedPreference {

	private int numbersOfErrorsBeforeApplyingAction;
	private ErrorExtractor errorExtractor;

	public RewardModificationOfModelPreference(int weight) {
		super(weight, PreferenceOption.REWARD_MODIFICATION_OF_MODEL);
		errorExtractor = new EcoreErrorExtractor();
	}

	@Override
	public void initializeBeforeApplyingAction(Model model) {
		numbersOfErrorsBeforeApplyingAction = errorExtractor.extractErrorsFrom(model.getRepresentationCopy(), false).size();
	}

	@Override
	int rewardActionForError(Model model, Error error, Action action) {
		int reward = 0;
		int numberOfErrorsAfter = errorExtractor.extractErrorsFrom(model.getRepresentationCopy(), false).size();

		if ((numbersOfErrorsBeforeApplyingAction - numberOfErrorsAfter) > 1) {
			reward = reward + (2 / 3 * weight * (numbersOfErrorsBeforeApplyingAction - numberOfErrorsAfter));
		} else {
			if ((numbersOfErrorsBeforeApplyingAction - numberOfErrorsAfter) != 0)
				reward = reward - weight;
		}
		return reward;
	}

}
