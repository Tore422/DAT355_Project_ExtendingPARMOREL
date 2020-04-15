package hvl.projectparmorel.reward;

import hvl.projectparmorel.ecore.EcoreErrorExtractor;
import hvl.projectparmorel.general.ErrorExtractor;
import hvl.projectparmorel.general.Model;

class PunishModificationOfModelPreference extends Preference implements ResultBasedPreference {

	private int numbersOfErrorsBeforeApplyingAction;
	private ErrorExtractor errorExtractor;
	
	public PunishModificationOfModelPreference(int weight) {
		super(weight, PreferenceValue.PUNISH_MODIFICATION_OF_MODEL);
	}

	@Override
	public void initializeBeforeApplyingAction(Model model) {
		switch(model.getModelType()) {
		case ECORE:
			errorExtractor = new EcoreErrorExtractor(model.getModelType().getUnsupportedErrorCodes());
			numbersOfErrorsBeforeApplyingAction = errorExtractor.extractErrorsFrom(model, false).size();
		}

	}

}
