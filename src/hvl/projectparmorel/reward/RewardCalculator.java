package hvl.projectparmorel.reward;

import java.util.ArrayList;
import java.util.List;
import hvl.projectparmorel.knowledge.ActionDirectory;
import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.knowledge.TagMap;
import hvl.projectparmorel.ml.Action;
import hvl.projectparmorel.ml.Error;
import hvl.projectparmorel.ml.Preferences;
import hvl.projectparmorel.ml.Sequence;

public class RewardCalculator {
	private TagMap tagMap;
	private Knowledge knowledge;
	private List<Integer> preferences;

	
	private int weightPunishDeletion;
	private int weightRewardRepairingHighInErrorHierarchies;
	private int weightRewardRepairingLowInErrorHierarchies;
	private int weightRewardModificationOfTheOriginalModel;
	private int weightPunishModificationOfTheOriginalModel;
	private int weightRewardShorterSequencesOfActions;
	private int weightRewardLongerSequencesOfActions;
	
	public RewardCalculator(Knowledge knowledge, List<Integer> preferences) {
		this.knowledge = knowledge;
		this.preferences = preferences;
		tagMap = new TagMap();
		
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
			if (action.getMsg().contains("delete")) {
				weight = -(double) weightPunishDeletion / 100;
			} else {
				weight = 0.0;
			}
		}

		if (action.getMsg().contains("get")) {
			weight = -10.0;
		} else {
			weight = 0.0;
		}

		return weight;
	}
	
	// Action rewards
		public int rewardCalculator(Error state, Action action) {
			int reward = 0;
			int num;

			if (action.getSubHierarchy() > -1) {
				num = Integer.valueOf(String.valueOf(action.getHierarchy()) + String.valueOf(action.getSubHierarchy()));
			} else {
				num = action.getHierarchy();
			}

			if (preferences.contains(2)) {
				if (action.getHierarchy() == 1) {
					reward += weightRewardRepairingHighInErrorHierarchies;
					addTagMap(state, num, action, 2, weightRewardRepairingHighInErrorHierarchies);
				} else if (action.getHierarchy() == 2) {
					reward += weightRewardRepairingHighInErrorHierarchies * 2 / 3;
					addTagMap(state, num, action, 2, weightRewardRepairingHighInErrorHierarchies * 2 / 3);
				} else if (action.getHierarchy() > 2) {
					reward -= -74 / 100 * weightRewardRepairingHighInErrorHierarchies;
					addTagMap(state, num, action, 2, -74 / 100 * weightRewardRepairingHighInErrorHierarchies);
				}
			}
			if (preferences.contains(3)) {
				if (action.getHierarchy() == 1) {
					reward -= 74 / 100 * weightRewardRepairingLowInErrorHierarchies;
					addTagMap(state, num, action, 3, -74 / 100 * weightRewardRepairingLowInErrorHierarchies);
				}
				if (action.getHierarchy() == 2) {
					reward += weightRewardRepairingLowInErrorHierarchies * 2 / 3;
					addTagMap(state, num, action, 3, weightRewardRepairingLowInErrorHierarchies * 2 / 3);
				}
				if (action.getHierarchy() > 2) {
					reward += weightRewardRepairingLowInErrorHierarchies;
					addTagMap(state, num, action, 3, weightRewardRepairingLowInErrorHierarchies);
				}
			}

			if (preferences.contains(4)) {
				if (action.getMsg().contains("delete")) {
					reward -= weightPunishDeletion;
					addTagMap(state, num, action, 4, -weightPunishDeletion);
				} else {
					reward += weightPunishDeletion / 10;
					addTagMap(state, num, action, 4, weightPunishDeletion / 10);
				}
			}

			if (!preferences.contains(2) && !preferences.contains(3) && !preferences.contains(4)) {
				reward += 30;
			}

			return reward;
		}
		
		private void addTagMap(Error error, int contextId, Action action, int tagId, int value) {
			tagMap.setTag(error.getCode(), contextId, action.getCode(), tagId, value);
		}

		public int updateBasedOnNumberOfErrors(int reward, int sizeBefore, int sizeAfter, Error currentErrorToFix, int code, Action action) {
			// check how the action has modified number of errors
			// high modification
			if (preferences.contains(6)) {
				if ((sizeBefore - sizeAfter) > 1) {
					reward = reward + (2 / 3 * weightRewardModificationOfTheOriginalModel
							* (sizeBefore - sizeAfter));
					addTagMap(currentErrorToFix, code, action, 6, (2 / 3
							* weightRewardModificationOfTheOriginalModel * (sizeBefore - sizeAfter)));
				} else {
					if ((sizeBefore - sizeAfter) != 0)
						reward = reward - weightRewardModificationOfTheOriginalModel;
					addTagMap(currentErrorToFix, code, action, 6, -weightRewardModificationOfTheOriginalModel);
				}
			}
			// low modification
			if (preferences.contains(5)) {
				if ((sizeBefore - sizeAfter) > 1) {
					reward = reward - (2 / 3 * weightPunishModificationOfTheOriginalModel
							* (sizeBefore - sizeAfter));
					addTagMap(currentErrorToFix, code, action, 5, -(2 / 3
							* weightPunishModificationOfTheOriginalModel * (sizeBefore - sizeAfter)));

				} else {
					if ((sizeBefore - sizeAfter) != 0)
						reward = reward + weightPunishModificationOfTheOriginalModel;
					addTagMap(currentErrorToFix, code, action, 5, weightPunishModificationOfTheOriginalModel);
				}
			}
			return reward;
		}
		
		public void updateSequencesWeights(Sequence s, int tag) {
			QTable qTable = knowledge.getQTable();
			ActionDirectory actionDirectory = knowledge.getActionDirectory();
			int num;
			for (int i = 0; i < s.getSeq().size(); i++) {
				if (s.getSeq().get(i).getAction().getSubHierarchy() > -1) {
					num = Integer.valueOf(String.valueOf(s.getSeq().get(i).getAction().getHierarchy())
							+ String.valueOf(s.getSeq().get(i).getAction().getSubHierarchy()));
				} else {
					num = s.getSeq().get(i).getAction().getHierarchy();
				}
				int errorCode = s.getSeq().get(i).getError().getCode();
				int actionId = s.getSeq().get(i).getAction().getCode();
				double oldWeight = qTable.getWeight(errorCode, num, actionId);

				qTable.setWeight(errorCode, num, actionId, oldWeight + 300);
				if (tag > -1) {

					if (!actionDirectory.getTagDictionaryForAction(errorCode, num, actionId).getTagDictionary()
							.containsKey(tag)) {
						actionDirectory.setTagValueInTagDictionary(errorCode, num, actionId, tag, 500);
					} else {
						int oldTagValue = actionDirectory.getTagDictionaryForAction(errorCode, num, actionId)
								.getTagDictionary().get(tag);
						actionDirectory.setTagValueInTagDictionary(errorCode, num, actionId, tag, oldTagValue + 500);
					}
				}
				
				tagMap.updateRewardInActionDirectory(actionDirectory, s.getSeq().get(i), num);	
			}
		}
		
		public void rewardSmallorBig(List<Sequence> sm) {
			int min = 9999;
			int max = 0;
			Sequence aux = null;

			if (preferences.contains(0)) {
				for (Sequence s : sm) {
					if (s.getSeq().size() < min && s.getWeight() > 0) {
						min = s.getSeq().size();
						aux = s;
					} else if (s.getSeq().size() == min) {
						if (s.getWeight() > aux.getWeight()) {
							aux = s;
						}
					}
				}
				aux.setWeight(aux.getWeight() + weightRewardShorterSequencesOfActions);
				updateSequencesWeights(aux, 0);
			}

			if (preferences.contains(1)) {
				for (Sequence s : sm) {
					if (s.getSeq().size() > max && s.getWeight() > 0) {
						max = s.getSeq().size();
						aux = s;
					} else if (s.getSeq().size() == max) {
						if (s.getWeight() > aux.getWeight()) {
							aux = s;
						}
					}
				}
				aux.setWeight(aux.getWeight() + weightRewardLongerSequencesOfActions);
				updateSequencesWeights(aux, 1);
			}
		}
		
		public int updateIfNewErrorIsIntroduced(int reward, List<Integer> originalCodes, Error next_state) {
			// if new error introduced
			if (!originalCodes.contains(next_state.getCode())) {
				// System.out.println("NEW ERROR: " + next_state.toString());
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
}
