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
	private List<Preference> preferences;
	private List<PreferenceOption> preferenceOptions;

	public RewardCalculator(Knowledge knowledge, List<PreferenceOption> preferences) {
		this.knowledge = knowledge;
		this.preferenceOptions = preferences;
		this.preferences = initializeFrom(preferences);
	}

	private List<Preference> initializeFrom(List<PreferenceOption> preferences) {
		Preferences filePreferences = new Preferences();
		
		List<Preference> prefs = new ArrayList<>();
		for (PreferenceOption preference : preferences) {
			switch (preference) {
			case SHORT_SEQUENCES_OF_ACTIONS:
				prefs.add(new PreferShortSequencesOfActions(filePreferences.getWeightRewardShorterSequencesOfActions()));
				break;
			case LONG_SEQUENCES_OF_ACTIONS:
				prefs.add(new PreferLongSequencesOfActions(filePreferences.getWeightRewardLongerSequencesOfActions()));
				break;
			case PUNISH_DELETION:
				prefs.add(new PunishDeletionPreference(filePreferences.getWeightPunishDeletion()));
				break;
			case REPAIR_HIGH_IN_CONTEXT_HIERARCHY:
				prefs.add(new PreferRepairingHighInContextHierarchyPreference(filePreferences.getWeightRewardRepairingHighInErrorHierarchies()));
				break;
			case REPAIR_LOW_IN_CONTEXT_HIERARCHY:
				prefs.add(new PreferRepairingLowInContextHierarchyPreference(filePreferences.getWeightRewardRepairingLowInErrorHierarchies()));
				break;
			case PUNISH_MODIFICATION_OF_MODEL:
				prefs.add(new PunishModificationOfModelPreference(filePreferences.getWeightPunishModificationOfTheOriginalModel()));
				break;
			case REWARD_MODIFICATION_OF_MODEL:
				prefs.add(new RewardModificationOfModelPreference(filePreferences.getWeightRewardModificationOfTheOriginalModel()));
				break;
			case PREFER_CLOSE_DISTANCE_TO_ORIGINAL:
				prefs.add(new PreferCloseDinstanceToOriginalPreference());
				break;
			case PREFER_MAINTAINABILITY:
				prefs.add(new PreferMaintainability());
				break;
			case PREFER_UNDERSTANDABILITY:
				prefs.add(new PreferUnderstandability());
				break;
			case PREFER_COMPLEXITY:
				prefs.add(new PreferComplexity());
				break;
			case PREFER_REUSE:
				prefs.add(new PreferReuse());
				break;
			case PREFER_RELAXATION:
				prefs.add(new PreferRelaxation());
				break;
			default:
				throw new UnsupportedOperationException("This operation is not yet implemented.");
			}
		}
		filePreferences.saveToFile();
		return prefs;
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
			if (preference instanceof InitializablePreference) {
				InitializablePreference pref = (InitializablePreference) preference;
				pref.initializeBeforeApplyingAction(model);
			}
		}
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

		int contextId = action.getContextId();
		for (Preference preference : preferences) {
			int rewardFromPreference = preference.rewardActionForError(model, currentErrorToFix, action);
			if (rewardFromPreference != 0) {
				addTagMap(currentErrorToFix, contextId, action, preference.getPreferenceOption().id,
						rewardFromPreference);
			}
			reward += rewardFromPreference;
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
		qTable.setTagValueInTagDictionary(error.getCode(), contextId, action.getId(), tagId, value);
	}
	
	/**
	 * Calculates rewards for the completed solution
	 * 
	 * @param episodeModel
	 * @param solution
	 * @return reward for solution
	 */
	public int calculateRewardFor(Model episodeModel, Solution solution) {
		QTable qTable = knowledge.getQTable();
		
		int reward = 0;
		if(solution != null) {
			for (Preference preference : preferences) {
				if(preference instanceof SolutionPreference) {
					SolutionPreference pref = (SolutionPreference) preference;
					reward += pref.rewardcalculateRewardFor(solution, episodeModel, qTable);
				}
			}
		}
		return reward;
	}

	/**
	 * Calculates rewards that compare the different solutions to each other.
	 * 
	 * @param possibleSolutions
	 */
	public void rewardPostRepair(List<Solution> possibleSolutions) {
		QTable qTable = knowledge.getQTable();
		
		for (Preference preference : preferences) {
			if (preference instanceof PostRepairPreference) {
				PostRepairPreference comparingPreference = (PostRepairPreference) preference;
				comparingPreference.rewardPostRepair(possibleSolutions, qTable);
			}
		}
	}

	/**
	 * Rewards the specified sequence.
	 * 
	 * @param solution
	 * @param preferenceId
	 */
	public void rewardSolution(Solution solution) {
		QTable qTable = knowledge.getQTable();
		for (AppliedAction appliedAction : solution.getSequence()) {
			int contextId = appliedAction.getAction().getContextId();
			int errorCode = appliedAction.getError().getCode();
			int actionId = appliedAction.getAction().getId();
			double oldWeight = qTable.getWeight(errorCode, contextId, actionId);

			qTable.setWeight(errorCode, contextId, actionId, oldWeight + 300);
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
	public void rewardSolution(Solution solution, boolean shouldSave) {
		rewardSolution(solution);
		if (shouldSave) {
			knowledge.save();
		}
	}

	public List<PreferenceOption> getPreferences() {
		return preferenceOptions;
	}

	public void influenceWeightsFromPreferencesBy(double factor) {
		knowledge.influenceWeightsFromPreferencesBy(factor, preferenceOptions);
	}
}
