package no.hvl.projectparmorel.qlearning.reward;

import no.hvl.projectparmorel.qlearning.Model;

public interface InitializablePreference {

	/**
	 * Some preferences compare aspects of the model pre and post repair. This call
	 * allows the preferences to store the required information from before the
	 * repair process begins.
	 * 
	 * @param model to extract information from.
	 */
	public void initializePreference(Model model);

	/**
	 * Some preferences compare aspects of the model pre and post applying an
	 * action. This call allows the preferences to store the required information
	 * before choosing action.
	 * 
	 * @param model to extract information from.
	 */
	public void initializeBeforeApplyingAction(Model model);
}
