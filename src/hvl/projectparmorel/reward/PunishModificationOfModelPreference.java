package hvl.projectparmorel.reward;

import hvl.projectparmorel.ecore.EcoreErrorExtractor;
import hvl.projectparmorel.general.ErrorExtractor;
import hvl.projectparmorel.general.Model;

class PunishModificationOfModelPreference extends Preference implements ResultBasedPreference {

	private int numbersOfErrorsBeforeApplyingAction;
	private ErrorExtractor errorExtractor;

	public PunishModificationOfModelPreference(int weight) {
		super(weight, PreferenceValue.PUNISH_MODIFICATION_OF_MODEL);
		errorExtractor = new EcoreErrorExtractor();
	}

	@Override
	public void initializeBeforeApplyingAction(Model model) {
		numbersOfErrorsBeforeApplyingAction = errorExtractor.extractErrorsFrom(model, false).size();
	}

}
