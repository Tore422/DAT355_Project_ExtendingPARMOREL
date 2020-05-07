package no.hvl.projectparmorel.qlearning.reward;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.ErrorExtractor;
import no.hvl.projectparmorel.qlearning.Model;
import no.hvl.projectparmorel.qlearning.ecore.EcoreErrorExtractor;

public class RewardModificationOfModelPreference extends Preference implements InitializablePreference {

	private int numbersOfErrorsBeforeApplyingAction;
	private ErrorExtractor errorExtractor;

	public RewardModificationOfModelPreference(int weight) {
		super(weight, PreferenceOption.REWARD_MODIFICATION_OF_MODEL);
	}
	
	@Override
	public void initializePreference(Model model) {
		switch (model.getModelType()) {
		case ECORE:
			errorExtractor = new EcoreErrorExtractor();
			break;
		default:
			throw new UnsupportedOperationException("This preference is not yet implemented for this model type.");
		}
	}

	@Override
	public void initializeBeforeApplyingAction(Model model) {
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
