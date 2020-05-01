package hvl.projectparmorel.reward;

import hvl.projectparmorel.ecore.EcoreErrorExtractor;
import hvl.projectparmorel.qlearning.Action;
import hvl.projectparmorel.qlearning.Error;
import hvl.projectparmorel.qlearning.ErrorExtractor;
import hvl.projectparmorel.qlearning.Model;

public class RewardModificationOfModelPreference extends Preference implements InitializablePreference {

	private int numbersOfErrorsBeforeApplyingAction;
	private ErrorExtractor errorExtractor;

	public RewardModificationOfModelPreference(int weight) {
		super(weight, PreferenceOption.REWARD_MODIFICATION_OF_MODEL);
	}

	@Override
	public void initializeBeforeApplyingAction(Model model) {
		switch (model.getModelType()) {
		case ECORE:
			errorExtractor = new EcoreErrorExtractor();
			break;
		default:
			throw new UnsupportedOperationException("This preference is not yet implemented for this model type.");
		}

		numbersOfErrorsBeforeApplyingAction = errorExtractor.extractErrorsFrom(model.getRepresentationCopy(), false)
				.size();
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
