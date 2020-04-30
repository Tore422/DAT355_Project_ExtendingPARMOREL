package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Model;

public interface InitializablePreference {
	/**
	 * Some preferences compare aspects of the model pre and post applying an
	 * action. This call allows the preferences to store the required information
	 * before choosing action.
	 * 
	 * @param model to extract information from.
	 */
	public void initializeBeforeApplyingAction(Model model);
}
