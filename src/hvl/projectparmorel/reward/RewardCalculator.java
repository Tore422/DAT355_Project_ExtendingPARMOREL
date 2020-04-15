package hvl.projectparmorel.reward;

import java.util.ArrayList;
import java.util.List;

import hvl.projectparmorel.modelrepair.Preferences;
import hvl.projectparmorel.modelrepair.Solution;
import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.knowledge.QTable;

public class RewardCalculator {
	private Knowledge knowledge;
	private List<Integer> preferenceNumbers;
	private List<Preference> preferences;

	private int weightRewardModificationOfTheOriginalModel;
	private int weightPunishModificationOfTheOriginalModel;

	public RewardCalculator(Knowledge knowledge, List<Integer> preferences) {
		this.knowledge = knowledge;
		this.preferenceNumbers = preferences;
		this.preferences = new ArrayList<>();

		Preferences prefs = new Preferences();

		Preference rewardShortActionSequences = new PreferShortSequencesOfActions(
				prefs.getWeightRewardShorterSequencesOfActions());
		Preference rewardLongActionSequences = new PreferLongSequencesOfActions(
				prefs.getWeightRewardLongerSequencesOfActions());
		Preference repairHighInHierarchy = new PreferRepairingHighInContextHierarchyPreference(
				prefs.getWeightRewardRepairingHighInErrorHierarchies());
		Preference repairLowInHierarchy = new PreferRepairingHighInContextHierarchyPreference(
				prefs.getWeightRewardRepairingLowInErrorHierarchies());
		Preference punishDeletion = new PunishDeletionPreference(prefs.getWeightPunishDeletion());
		Preference punishModification = new PunishModificationOfModelPreference(
				prefs.getWeightPunishModificationOfTheOriginalModel());
		Preference rewardModification = new RewardModificationOfModelPreference(
				prefs.getWeightRewardModificationOfTheOriginalModel());

		if (preferences.contains(0))
			this.preferences.add(rewardShortActionSequences);
		if (preferences.contains(1))
			this.preferences.add(rewardLongActionSequences);
		if (preferences.contains(2))
			this.preferences.add(repairHighInHierarchy);
		if (preferences.contains(3))
			this.preferences.add(repairLowInHierarchy);
		if (preferences.contains(4))
			this.preferences.add(punishDeletion);
		if (preferences.contains(5))
			this.preferences.add(punishModification);
		if (preferences.contains(6))
			this.preferences.add(rewardModification);

		knowledge = new hvl.projectparmorel.knowledge.Knowledge(); // preferences);
	
		weightPunishModificationOfTheOriginalModel = prefs.getWeightPunishModificationOfTheOriginalModel();
		weightRewardModificationOfTheOriginalModel = prefs.getWeightRewardModificationOfTheOriginalModel();
		prefs.saveToFile();
	}

	/**
	 * Calculates the reward based on the result from applying the specified action
	 * to the specified error to fix.
	 * 
	 * @param currentErrorToFix
	 * @param action
	 * @return the reward
	 */
	public int calculateRewardFor(Model model, Error currentErrorToFix, Action action) {
		int reward = 0;

		int contextId = action.getHierarchy();
		for (Preference preference : preferences) {
			int rewardFromPreference = preference.rewardActionForError(model, currentErrorToFix, action);
			if (rewardFromPreference != 0) {
				addTagMap(currentErrorToFix, contextId, action, preference.getPreferenceValue().id,
						rewardFromPreference);
			}
			reward += rewardFromPreference;
		}

		if (!preferenceNumbers.contains(2) && !preferenceNumbers.contains(3) && !preferenceNumbers.contains(4)) {
			reward += 30;
		}

		return reward;
	}

	/**
	 * Sets the tag map for the error, context and action to the specified tagId and
	 * value
	 * 
	 * @param error
	 * @param contextId
	 * @param action
	 * @param tagId
	 * @param value
	 */
	private void addTagMap(Error error, int contextId, Action action, int tagId, int value) {
		QTable qTable = knowledge.getQTable();
		qTable.setTagValueInTagDictionary(error.getCode(), contextId, action.getCode(), tagId, value);
	}

	/**
	 * Rewards the specified sequence.
	 * 
	 * @param solution
	 * @param preferenceId
	 */
	public void rewardSolution(Solution solution, int preferenceId) {
		QTable qTable = knowledge.getQTable();
		for (AppliedAction appliedAction : solution.getSequence()) {
			int contextId = appliedAction.getAction().getHierarchy();
			int errorCode = appliedAction.getError().getCode();
			int actionId = appliedAction.getAction().getCode();
			double oldWeight = qTable.getWeight(errorCode, contextId, actionId);

			qTable.setWeight(errorCode, contextId, actionId, oldWeight + 300);
			if (preferenceId > -1) {
				if (qTable.getTagDictionaryForAction(errorCode, contextId, actionId).contains(preferenceId)) {
					int oldTagValue = qTable.getTagDictionaryForAction(errorCode, contextId, actionId)
							.getWeightFor(preferenceId);
					qTable.setTagValueInTagDictionary(errorCode, contextId, actionId, preferenceId, oldTagValue + 500);
				} else {
					qTable.setTagValueInTagDictionary(errorCode, contextId, actionId, preferenceId, 500);
				}
			}
			qTable.updateReward(appliedAction, contextId);
		}
	}

	/**
	 * Rewards the specified sequence. Saves the knowledge afterwards if the
	 * shouldSave-variable is set to true.
	 * 
	 * @param solution
	 * @param preferenceId
	 * @param shouldSave
	 */
	public void rewardSolution(Solution solution, int preferenceId, boolean shouldSave) {
		rewardSolution(solution, preferenceId);
		if (shouldSave) {
			knowledge.save();
		}
	}

//	public int updateIfNewErrorIsIntroduced(int reward, List<Integer> originalCodes, Error nextErrorToFix) {
//		// if new error introduced
//		if (!originalCodes.contains(nextErrorToFix.getCode())) {
//			// high modification
//			if (preferenceNumbers.contains(6)) {
//				reward = reward + 2 / 3 * weightRewardModificationOfTheOriginalModel;
//			}
//			// low modification
//			if (preferenceNumbers.contains(5)) {
//				reward = reward - 2 / 3 * weightPunishModificationOfTheOriginalModel;
//			}
//		}
//		return reward;
//	}

	public List<Integer> getPreferences() {
		return new ArrayList<Integer>(preferenceNumbers);
	}

	public void influenceWeightsFromPreferencesBy(double factor) {
		knowledge.influenceWeightsFromPreferencesBy(factor, preferenceNumbers);
	}

	/**
	 * Some preferences compare aspects of the model pre and post applying an
	 * action. This call allows the preferences to store the required information
	 * before choosing action.
	 * 
	 * @param model
	 */
	public void initializePreferencesBeforeChoosingAction(Model model) {
		for (Preference preference : preferences) {
			if (preference instanceof ResultBasedPreference) {
				ResultBasedPreference pref = (ResultBasedPreference) preference;
				pref.initializeBeforeApplyingAction(model);
			}

		}

	}

	/**
	 * Calculates rewards that compare the different solutions to each other.
	 * 
	 * @param possibleSolutions
	 */
	public void rewardPostRepair(List<Solution> possibleSolutions) {
		for (Preference preference : preferences) {
			if (preference instanceof PostRepairPreference) {
				PostRepairPreference comparingPreference = (PostRepairPreference) preference;
				comparingPreference.rewardPostRepair(possibleSolutions, knowledge);
			}
		}
	}
}
