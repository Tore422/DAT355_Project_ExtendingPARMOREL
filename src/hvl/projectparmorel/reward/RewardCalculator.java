package hvl.projectparmorel.reward;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hvl.projectparmorel.knowledge.ActionDirectory;
import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.ml.Action;
import hvl.projectparmorel.ml.Error;
import hvl.projectparmorel.ml.QLearning;
import hvl.projectparmorel.ml.Sequence;

public class RewardCalculator {
	
	private Map<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> tagMap = new HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>();
	private Knowledge knowledge;
	
	private int weightPunishDeletion;
	private int weightRewardRepairingHighInErrorHierarchies;
	private int weightRewardRepairingLowInErrorHierarchies;
	private int weightRewardModificationOfTheOriginalModel;
	private int weightPunishModificationOfTheOriginalModel;
	private int weightRewardShorterSequencesOfActions;
	private int weightRewardLongerSequencesOfActions;
	
	public RewardCalculator(Knowledge knowledge, int weightPunishDeletion, int weightRewardRepairingHighInErrorHierarchies, int weightRewardRepairingLowInErrorHierarchies, int weightRewardModificationOfTheOriginalModel, int weightPunishModificationOfTheOriginalModel, int weightRewardShorterSequencesOfActions, int weightRewardLongerSequencesOfActions) {
		this.knowledge = knowledge;
		this.weightPunishDeletion = weightPunishDeletion;
		this.weightRewardRepairingHighInErrorHierarchies = weightRewardRepairingHighInErrorHierarchies;
		this.weightRewardRepairingLowInErrorHierarchies = weightRewardRepairingLowInErrorHierarchies;
		this.weightRewardModificationOfTheOriginalModel = weightRewardModificationOfTheOriginalModel;
		this.weightPunishModificationOfTheOriginalModel = weightPunishModificationOfTheOriginalModel;
		this.weightRewardShorterSequencesOfActions = weightRewardShorterSequencesOfActions;
		this.weightRewardLongerSequencesOfActions = weightRewardLongerSequencesOfActions;
	}

	/**
	 * Initializes the weight for the given action
	 * 
	 * @param action
	 * @return initial weight
	 */
	public double initializeWeightFor(Action action) {
		double weight = 0.0;

		if (QLearning.preferences.contains(4)) {
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

			if (QLearning.preferences.contains(2)) {
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
			if (QLearning.preferences.contains(3)) {
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

			if (QLearning.preferences.contains(4)) {
				if (action.getMsg().contains("delete")) {
					reward -= weightPunishDeletion;
					addTagMap(state, num, action, 4, -weightPunishDeletion);
				} else {
					reward += weightPunishDeletion / 10;
					addTagMap(state, num, action, 4, weightPunishDeletion / 10);
				}
			}

			if (!QLearning.preferences.contains(2) && !QLearning.preferences.contains(3) && !QLearning.preferences.contains(4)) {
				reward += 30;
			}

			return reward;
		}
		
		private void addTagMap(Error state, int num, Action action, int tag, int r) {
			HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> hashaux = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
			HashMap<Integer, HashMap<Integer, Integer>> hashaux2 = new HashMap<Integer, HashMap<Integer, Integer>>();
			HashMap<Integer, Integer> hashaux3 = new HashMap<Integer, Integer>();

			if (!tagMap.containsKey(state.getCode())) {
				hashaux3.put(tag, r);
				hashaux2.put(action.getCode(), hashaux3);
				hashaux.put(num, hashaux2);
				tagMap.put(state.getCode(), hashaux);
			}
			if (!tagMap.get(state.getCode()).containsKey(num)) {
				hashaux3.put(tag, r);
				hashaux2.put(action.getCode(), hashaux3);
				tagMap.get(state.getCode()).put(num, hashaux2);
			}
			if (!tagMap.get(state.getCode()).get(num).containsKey(action.getCode())) {
				hashaux3.put(tag, r);
				tagMap.get(state.getCode()).get(num).put(action.getCode(), hashaux3);
			}
			if (!tagMap.get(state.getCode()).get(num).get(action.getCode()).containsKey(tag)) {
				tagMap.get(state.getCode()).get(num).get(action.getCode()).put(tag, r);
			} else {
				tagMap.get(state.getCode()).get(num).get(action.getCode()).put(tag,
						r + tagMap.get(state.getCode()).get(num).get(action.getCode()).get(tag));
			}
		}

		public int updateBasedOnNumberOfErrors(int reward, int sizeBefore, int sizeAfter, Error currentErrorToFix, int code, Action action) {
			// check how the action has modified number of errors
			// high modification
			if (QLearning.preferences.contains(6)) {
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
			if (QLearning.preferences.contains(5)) {
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

				if (tagMap.containsKey(s.getSeq().get(i).getError().getCode())) {
					if (tagMap.get(s.getSeq().get(i).getError().getCode()).containsKey(num)) {
						if (tagMap.get(s.getSeq().get(i).getError().getCode()).get(num)
								.containsKey(s.getSeq().get(i).getAction().getCode())) {
							for (Integer key : tagMap.get(s.getSeq().get(i).getError().getCode()).get(num)
									.get(s.getSeq().get(i).getAction().getCode()).keySet()) {
								if (!actionDirectory.getTagDictionaryForAction(errorCode, num, actionId).getTagDictionary()
										.containsKey(key)) {
									int newTagValue = tagMap.get(s.getSeq().get(i).getError().getCode()).get(num)
											.get(s.getSeq().get(i).getAction().getCode()).get(key);
									actionDirectory.setTagValueInTagDictionary(errorCode, num, actionId, key, newTagValue);

								} else {
									int newTagValue = actionDirectory.getTagDictionaryForAction(errorCode, num, actionId)
											.getTagDictionary().get(key)
											+ tagMap.get(s.getSeq().get(i).getError().getCode()).get(num)
													.get(s.getSeq().get(i).getAction().getCode()).get(key);
									actionDirectory.setTagValueInTagDictionary(errorCode, num, actionId, key, newTagValue);
								}
							}
						}
					}
				}
			}

		}
		
		public void rewardSmallorBig(List<Sequence> sm) {
			int min = 9999;
			int max = 0;
			Sequence aux = null;

			if (QLearning.preferences.contains(0)) {
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

			if (QLearning.preferences.contains(1)) {
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
}
