package hvl.projectparmorel.reward;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import hvl.projectparmorel.modelrepair.Preferences;
import hvl.projectparmorel.modelrepair.Solution;
import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.knowledge.QTable;

public class RewardCalculator {
	private Knowledge knowledge;
	private List<Integer> preferences;

	private int weightPunishDeletion;
	private int weightRewardRepairingHighInErrorHierarchies;
	private int weightRewardRepairingLowInErrorHierarchies;
	private int weightRewardModificationOfTheOriginalModel;
	private int weightPunishModificationOfTheOriginalModel;
	private int weightRewardShorterSequencesOfActions;
	private int weightRewardLongerSequencesOfActions;
	
	private Logger logger;

	public RewardCalculator(Knowledge knowledge, List<Integer> preferences) {
		this.knowledge = knowledge;
		this.preferences = preferences;

		Preferences prefs = new Preferences();
		knowledge = new hvl.projectparmorel.knowledge.Knowledge(); // preferences);
		weightRewardShorterSequencesOfActions = prefs.getWeightRewardShorterSequencesOfActions();
		weightRewardLongerSequencesOfActions = prefs.getWeightRewardLongerSequencesOfActions();
		weightRewardRepairingHighInErrorHierarchies = prefs.getWeightRewardRepairingHighInErrorHierarchies();
		weightRewardRepairingLowInErrorHierarchies = prefs.getWeightRewardRepairingHighInErrorHierarchies();
		weightPunishDeletion = prefs.getWeightPunishDeletion();
		weightPunishModificationOfTheOriginalModel = prefs.getWeightPunishModificationOfTheOriginalModel();
		weightRewardModificationOfTheOriginalModel = prefs.getWeightRewardModificationOfTheOriginalModel();
		prefs.saveToFile();
		
		logger = Logger.getLogger("MyLog");
	}

	/**
	 * Initializes the weight for the given action
	 * 
	 * @param action
	 * @return initial weight
	 */
	public double initializeWeightFor(Action action) {
		double weight = 0.0;

		if (preferences.contains(4)) {
			if (action.getMessage().contains("delete")) {
				weight = -(double) weightPunishDeletion / 100;
			} else {
				weight = 0.0;
			}
		}

		if (action.getMessage().contains("get")) {
			weight = -10.0;
		} else {
			weight = 0.0;
		}

		return weight;
	}

	/**
	 * Calculates the reward based on the result from applying the specified action
	 * to the specified error to fix.
	 * 
	 * @param currentErrorToFix
	 * @param action
	 * @return the reward
	 */
	public int calculateRewardFor(Error currentErrorToFix, Action action) {
		int reward = 0;
		int contextId = action.getHierarchy();

		if (preferences.contains(2)) {
			if (action.getHierarchy() == 1) {
				reward += weightRewardRepairingHighInErrorHierarchies;
				addTagMap(currentErrorToFix, contextId, action, 2, weightRewardRepairingHighInErrorHierarchies);
			} else if (action.getHierarchy() == 2) {
				reward += weightRewardRepairingHighInErrorHierarchies * 2 / 3;
				addTagMap(currentErrorToFix, contextId, action, 2, weightRewardRepairingHighInErrorHierarchies * 2 / 3);
			} else if (action.getHierarchy() > 2) {
				reward -= -74 / 100 * weightRewardRepairingHighInErrorHierarchies;
				addTagMap(currentErrorToFix, contextId, action, 2,
						-74 / 100 * weightRewardRepairingHighInErrorHierarchies);
			}
		}
		if (preferences.contains(3)) {
			if (action.getHierarchy() == 1) {
				reward -= 74 / 100 * weightRewardRepairingLowInErrorHierarchies;
				addTagMap(currentErrorToFix, contextId, action, 3,
						-74 / 100 * weightRewardRepairingLowInErrorHierarchies);
			}
			if (action.getHierarchy() == 2) {
				reward += weightRewardRepairingLowInErrorHierarchies * 2 / 3;
				addTagMap(currentErrorToFix, contextId, action, 3, weightRewardRepairingLowInErrorHierarchies * 2 / 3);
			}
			if (action.getHierarchy() > 2) {
				reward += weightRewardRepairingLowInErrorHierarchies;
				addTagMap(currentErrorToFix, contextId, action, 3, weightRewardRepairingLowInErrorHierarchies);
			}
		}

		if (preferences.contains(4)) {
			if (action.getMessage().contains("delete")) {
				reward -= weightPunishDeletion;
				addTagMap(currentErrorToFix, contextId, action, 4, -weightPunishDeletion);
			} else {
				reward += weightPunishDeletion / 10;
				addTagMap(currentErrorToFix, contextId, action, 4, weightPunishDeletion / 10);
			}
		}

		if (!preferences.contains(2) && !preferences.contains(3) && !preferences.contains(4)) {
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
	 * Updates the reward given as input based on the change in numbers of errors
	 * 
	 * @param reward
	 * @param sizeBefore
	 * @param sizeAfter
	 * @param currentErrorToFix
	 * @param code
	 * @param action
	 * @return the updated weight
	 */
	public int updateBasedOnNumberOfErrors(int reward, int sizeBefore, int sizeAfter, Error currentErrorToFix, int code,
			Action action) {
		// check how the action has modified number of errors
		// high modification
		if (preferences.contains(6)) {
			if ((sizeBefore - sizeAfter) > 1) {
				reward = reward + (2 / 3 * weightRewardModificationOfTheOriginalModel * (sizeBefore - sizeAfter));
				addTagMap(currentErrorToFix, code, action, 6,
						(2 / 3 * weightRewardModificationOfTheOriginalModel * (sizeBefore - sizeAfter)));
			} else {
				if ((sizeBefore - sizeAfter) != 0)
					reward = reward - weightRewardModificationOfTheOriginalModel;
				addTagMap(currentErrorToFix, code, action, 6, -weightRewardModificationOfTheOriginalModel);
			}
		}
		// low modification
		if (preferences.contains(5)) {
			if ((sizeBefore - sizeAfter) > 1) {
				reward = reward - (2 / 3 * weightPunishModificationOfTheOriginalModel * (sizeBefore - sizeAfter));
				addTagMap(currentErrorToFix, code, action, 5,
						-(2 / 3 * weightPunishModificationOfTheOriginalModel * (sizeBefore - sizeAfter)));

			} else {
				if ((sizeBefore - sizeAfter) != 0)
					reward = reward + weightPunishModificationOfTheOriginalModel;
				addTagMap(currentErrorToFix, code, action, 5, weightPunishModificationOfTheOriginalModel);
			}
		}
		return reward;
	}

	/**
	 * Rewards the specified sequence.
	 * 
	 * @param solution
	 * @param preferenceId
	 */
	public void rewardSolution(Solution solution, int preferenceId) {
		QTable qTable = knowledge.getQTable();
		for (int i = 0; i < solution.getSequence().size(); i++) {
			int contextId = solution.getSequence().get(i).getAction().getHierarchy();
			int errorCode = solution.getSequence().get(i).getError().getCode();
			int actionId = solution.getSequence().get(i).getAction().getCode();
			double oldWeight = qTable.getWeight(errorCode, contextId, actionId);

			qTable.setWeight(errorCode, contextId, actionId, oldWeight + 300);
			if (preferenceId > -1) {
				if (!qTable.getTagDictionaryForAction(errorCode, contextId, actionId).contains(preferenceId)) {
					qTable.setTagValueInTagDictionary(errorCode, contextId, actionId, preferenceId, 500);
				} else {
					int oldTagValue = qTable.getTagDictionaryForAction(errorCode, contextId, actionId)
							.getWeightFor(preferenceId);
					qTable.setTagValueInTagDictionary(errorCode, contextId, actionId, preferenceId, oldTagValue + 500);
				}
			}
			qTable.updateReward(solution.getSequence().get(i), contextId);
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

	/**
	 * Rewards the sequences based on their length
	 * 
	 * @param sequences
	 */
	public void rewardBasedOnSequenceLength(List<Solution> sequences) {
		if (preferences.contains(0)) {
			handlePreferShortSequences(sequences);
		}
		if (preferences.contains(1)) {
			handlePreferLongSequences(sequences);
		}
	}

	/**
	 * Rewards the best shortest sequence in the specified list of sequences
	 * 
	 * @param sequences
	 */
	private void handlePreferShortSequences(List<Solution> sequences) {
		Solution optimalSequence = null;
		int smallestSequenceSize = 9999;

		for (Solution sequence : sequences) {
			if (sequence.getSequence().size() < smallestSequenceSize && sequence.getWeight() > 0) {
				smallestSequenceSize = sequence.getSequence().size();
				optimalSequence = sequence;
			} else if (sequence.getSequence().size() == smallestSequenceSize) {
				if (sequence.getWeight() > optimalSequence.getWeight()) {
					optimalSequence = sequence;
				}
			}
		}
		if (optimalSequence != null) {
			optimalSequence.setWeight(optimalSequence.getWeight() + weightRewardShorterSequencesOfActions);
			rewardSolution(optimalSequence, 0);
			logger.info("Rewarded solution " + optimalSequence.getId() + " with a weight of " + weightRewardShorterSequencesOfActions + " because of preferences to reward shorter sequences.");
		}
	}

	/**
	 * Rewards the best longest sequence in the specified list of sequences
	 * 
	 * @param sequences
	 */
	private void handlePreferLongSequences(List<Solution> sequences) {
		Solution optimalSequence = null;
		int largestSequenceSize = 0;

		for (Solution sequence : sequences) {
			if (sequence.getSequence().size() > largestSequenceSize && sequence.getWeight() > 0) {
				largestSequenceSize = sequence.getSequence().size();
				optimalSequence = sequence;
			} else if (sequence.getSequence().size() == largestSequenceSize) {
				if (sequence.getWeight() > optimalSequence.getWeight()) {
					optimalSequence = sequence;
				}
			}
		}
		if (optimalSequence != null) {
			optimalSequence.setWeight(optimalSequence.getWeight() + weightRewardLongerSequencesOfActions);
			rewardSolution(optimalSequence, 1);
			logger.info("Rewarded solution " + optimalSequence.getId() + " with a weight of " + weightRewardLongerSequencesOfActions + " because of preferences to reward longer sequences.");
		}
	}

	public int updateIfNewErrorIsIntroduced(int reward, List<Integer> originalCodes, Error nextErrorToFix) {
		// if new error introduced
		if (!originalCodes.contains(nextErrorToFix.getCode())) {
			// high modification
			if (preferences.contains(6)) {
				reward = reward + 2 / 3 * weightRewardModificationOfTheOriginalModel;
			}
			// low modification
			if (preferences.contains(5)) {
				reward = reward - 2 / 3 * weightPunishModificationOfTheOriginalModel;
			}
		}
		return reward;
	}

	public List<Integer> getPreferences() {
		return new ArrayList<Integer>(preferences);
	}

	public void influenceWeightsFromPreferencesBy(double factor) {
		knowledge.influenceWeightsFromPreferencesBy(factor, preferences);
	}
}
