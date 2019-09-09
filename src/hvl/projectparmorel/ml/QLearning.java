package hvl.projectparmorel.ml;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import hvl.projectparmorel.knowledge.ActionDirectory;
import hvl.projectparmorel.knowledge.QTable;

/**
 * Western Norway University of Applied Sciences Bergen, Norway
 * 
 * @author Angela Barriga Rodriguez abar@hvl.no
 * @author Magnus Marthinsen
 */
public class QLearning {
	private hvl.projectparmorel.knowledge.Knowledge knowledge;

	protected static int N_EPISODES = 25;
	protected static double randomfactor = 0.25;

	private List<Error> errorsToFix;

	private Logger logger = Logger.getGlobal();

	private final double MIN_ALPHA = 0.06; // Learning rate
	private final double gamma = 1.0; // Eagerness - 0 looks in the near future, 1 looks in the distant future
	private int reward = 0;
	int total_reward = 0;
	public URI uri;
	List<Error> original = new ArrayList<Error>();
	public List<Integer> originalCodes = new ArrayList<Integer>();
	List<Sequence> solvingMap = new ArrayList<Sequence>();
	Map<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> tagMap = new HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>();
	public ResourceSet resourceSet = new ResourceSetImpl();

	int MAX_EPISODE_STEPS = 20;
	public Resource myMetaModel;
	public static int user;

	Sequence sx;
	public static List<Integer> preferences = new ArrayList<Integer>();

	private int weightRewardShorterSequencesOfActions;
	private int weightRewardLongerSequencesOfActions;
	private int weightRewardRepairingHighInErrorHierarchies;
	private int weightRewardRepairingLowInErrorHierarchies;
	protected static int weightPunishDeletion;
	private int weightPunishModificationOfTheOriginalModel;
	private int weightRewardModificationOfTheOriginalModel;

	public QLearning() {
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

		errorsToFix = new ArrayList<Error>();
	}

//	/**
//	 * Saves the knowledge
//	 */
//	public void saveKnowledge() {
//		knowledge.save();
//	}

	public static double[] linspace(double min, double max, int points) {
		double[] d = new double[points];
		for (int i = 0; i < points; i++) {
			d[i] = min + i * (max - min) / (points - 1);
		}
		return d;
	}

	double[] alphas = linspace(1.0, MIN_ALPHA, N_EPISODES);

	public Sequence getBestSeq() {
		return sx;
	}

	public void setBestSeq(Sequence sx) {
		this.sx = sx;
	}

	/**
	 * Chooses an action for the specified error. The action is either the best
	 * action based on the previous knowledge, or a random action.
	 * 
	 * @param error
	 * @return a fitting action
	 */
	private Action chooseAction(Error error) {
		if (Math.random() < randomfactor) {
			return knowledge.getActionDirectory().getRandomActionForError(error.getCode());
		} else {
			return knowledge.getOptimalActionForErrorCode(error.getCode());
		}
	}

	public void modelFixer(Resource auxModel) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		QTable qTable = knowledge.getQTable();
		ActionExtractor actionExtractor = new ActionExtractor(knowledge);

		int val;
		int discarded = 0;
		int episode = 0;
		boolean nope = false;
		boolean alert = false;
		int code, code2;
		Error next_state;

		Resource modelCopy = copy(myMetaModel, uri);
		errorsToFix = ErrorExtractor.extractErrorsFrom(modelCopy);
		solvingMap.clear();
		original.clear();
		original.addAll(errorsToFix);

		// FILTER ACTIONS AND INITIALICES QTABLE
		ModelProcesser modelProcesser = new ModelProcesser(resourceSet, knowledge);
		modelProcesser.initializeQTableForErrorsInModel(modelCopy, uri);
		// START with initial model its errors and actions
		logger.info("Errors to fix: " + errorsToFix.toString());
		logger.info("Number of episodes: " + N_EPISODES);
		while (episode < N_EPISODES) {
			int index = 0;
			Error currentErrorToFix = errorsToFix.get(index);
			int sizeBefore = errorsToFix.size();
			total_reward = 0;
			double alpha = alphas[episode];
			int end_reward = 0;
			int step = 0;
			boolean doni = false;
			Sequence s = new Sequence();
			while (step < MAX_EPISODE_STEPS) {
				Action action = chooseAction(currentErrorToFix);

				errorsToFix.clear();
				errorsToFix = modelProcesser.tryApplyAction(currentErrorToFix, action, modelCopy,
						action.getHierarchy()); // removed subHirerarchy - effect?
				reward = rewardCalculator(currentErrorToFix, action);
				// Insert stuff into sequence
				s.setId(episode);
				List<ErrorAction> ea = s.getSeq();
				ea.add(new ErrorAction(currentErrorToFix, action));
				if ((currentErrorToFix.getCode() == 401 || currentErrorToFix.getCode() == 445
						|| currentErrorToFix.getCode() == 27 || currentErrorToFix.getCode() == 32)
						&& (action.getMsg().contentEquals("setEType") || action.getMsg().contentEquals("delete")
								|| action.getMsg().contentEquals("setName")
								|| action.getMsg().contentEquals("unsetEGenericType"))) {
					alert = true;
				}
				s.setSeq(ea);
				s.setU(uri);

				if (action.getSubHierarchy() != -1) {
					code = Integer
							.valueOf(String.valueOf(action.getHierarchy()) + String.valueOf(action.getSubHierarchy()));
				} else {
					code = action.getHierarchy();
				}

				// check how the action has modified number of errors
				// high modification
				if (preferences.contains(6)) {
					if ((sizeBefore - errorsToFix.size()) > 1) {
						reward = reward + (2 / 3 * weightRewardModificationOfTheOriginalModel
								* (sizeBefore - errorsToFix.size()));
						addTagMap(currentErrorToFix, code, action, 6, (2 / 3
								* weightRewardModificationOfTheOriginalModel * (sizeBefore - errorsToFix.size())));
					} else {
						if ((sizeBefore - errorsToFix.size()) != 0)
							reward = reward - weightRewardModificationOfTheOriginalModel;
						addTagMap(currentErrorToFix, code, action, 6, -weightRewardModificationOfTheOriginalModel);
					}
				}
				// low modification
				if (preferences.contains(5)) {
					if ((sizeBefore - errorsToFix.size()) > 1) {
						reward = reward - (2 / 3 * weightPunishModificationOfTheOriginalModel
								* (sizeBefore - errorsToFix.size()));
						addTagMap(currentErrorToFix, code, action, 5, -(2 / 3
								* weightPunishModificationOfTheOriginalModel * (sizeBefore - errorsToFix.size())));

					} else {
						if ((sizeBefore - errorsToFix.size()) != 0)
							reward = reward + weightPunishModificationOfTheOriginalModel;
						addTagMap(currentErrorToFix, code, action, 5, weightPunishModificationOfTheOriginalModel);
					}
				}

				if (errorsToFix.size() != 0) {
					next_state = errorsToFix.get(index);

					if (!qTable.containsErrorCode(next_state.getCode())) {
//					if (!processed.contains(next_state.getCode())
//							|| !experience.getqTable().containsKey(next_state.getCode())) {
						errorsToFix = ErrorExtractor.extractErrorsFrom(modelCopy);
						actionExtractor.extractActionsFor(errorsToFix);
						modelProcesser.initializeQTableForErrorsInModel(modelCopy, uri);
					}
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

					next_state = errorsToFix.get(index);
					Action a = knowledge.getOptimalActionForErrorCode(next_state.getCode());

					if (a.getSubHierarchy() != -1) {
						code2 = Integer.valueOf(String.valueOf(a.getHierarchy()) + String.valueOf(a.getSubHierarchy()));
					} else {
						code2 = a.getHierarchy();
					}
					double value = qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode())
							+ alpha * (reward + gamma * qTable.getWeight(next_state.getCode(), code2, a.getCode()))
							- qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode());

//					double value = experience.getqTable().get(state.getCode()).get(code).get(action.getCode())
//							+ alpha * (reward
//									+ gamma * experience.getqTable().get(next_state.getCode()).get(code2)
//											.get(a.getCode())
//									- experience.getqTable().get(state.getCode()).get(code).get(action.getCode()));

					qTable.setWeight(currentErrorToFix.getCode(), code, action.getCode(), value);
//					experience.getqTable().get(state.getCode()).get(code).put(action.getCode(), value);
					currentErrorToFix = next_state;
					sizeBefore = errorsToFix.size();
				} // it has reached the end

				else {
					end_reward = 1;

					double value = qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode())
							+ alpha * (reward + gamma * end_reward)
							- qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode());

//					double value = experience.getqTable().get(state.getCode()).get(code).get(action.getCode())
//							+ alpha * (reward + gamma * end_reward)
//							- experience.getqTable().get(state.getCode()).get(code).get(action.getCode());
					qTable.setWeight(currentErrorToFix.getCode(), code, action.getCode(), value);
//					experience.getqTable().get(state.getCode()).get(code).put(action.getCode(), value);
					doni = true;
				}

				total_reward = total_reward + reward;

				if (doni) {
					break;
				}

				step++;
			}
			// add the whole sequence into list

			if (alert) {
				try {
					s.setModel(modelCopy);
				} catch (java.lang.NullPointerException exception) {
					// Catch NullPointerExceptions.
					nope = true;
				}
			} else {
				s.setModel(modelCopy);
			}

			if (s.getSeq().size() > 7) {
				val = loopChecker(s.getSeq());
				if (val > 1) {
//					total_reward = total_reward - val * 1000;
				}
			}

			s.setWeight(total_reward);

			if (!nope && uniqueSequence(s)) {
				// System.out.println(s.toString());
				solvingMap.add(s);
			} else {
				discarded++;
			}

			// RESET initial model and extract actions + errors
			modelCopy.getContents().clear();
			modelCopy.getContents().add(EcoreUtil.copy(myMetaModel.getContents().get(0)));
			logger.info("EPISODE " + episode + " TOTAL REWARD " + total_reward);
			errorsToFix.clear();
			errorsToFix.addAll(original);

			episode++;
			nope = false;
			alert = false;

		}
		setBestSeq(bestSequence(solvingMap));

		logger.info("\n-----------------ALL SEQUENCES FOUND-------------------" + "\nSIZE: " + solvingMap.size()
				+ "\nDISCARDED SEQUENCES: " + discarded + "\n--------::::B E S T   S E Q U E N C E   I S::::---------\n"
				+ getBestSeq().toString());

		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// THIS SAVES THE REPAIRED MODEL
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if (getBestSeq().getSeq().size() != 0) {
			updateSequencesWeights(getBestSeq(), -1);
			sx.getModel().save(null);
		}
	}

	/**
	 * Copies the model passed as a parameter
	 * 
	 * @param model
	 * @param       uri, the Uniform Resource Identifier for the copy
	 * @return a copy
	 */
	private Resource copy(Resource model, URI uri) {
		Resource modelCopy = resourceSet.createResource(uri);
		modelCopy.getContents().add(EcoreUtil.copy(model.getContents().get(0)));
		return modelCopy;
	}

	void updateSequencesWeights(Sequence s, int tag) {
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

	void rewardSmallorBig(List<Sequence> sm) {
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

	boolean uniqueSequence(Sequence s) {
		boolean check = true;
		int same = 0;
		for (Sequence seq : solvingMap) {
			if (seq.getWeight() == s.getWeight()) {
				for (ErrorAction ea : s.getSeq()) {
					for (ErrorAction ea2 : seq.getSeq()) {
						if (ea.equals(ea2)) {
							same++;
						}
					}
				} // for ea
					// if all elements in list are the same
				if (same == s.getSeq().size()) {
					check = false;
					break;
				}
			} // if weight
		} // for
		return check;
	}

	boolean checkWeight(Sequence s) {
		boolean check = false;
		for (Sequence seq : solvingMap) {
			if (seq.getWeight() == s.getWeight()) {
				check = true;
				break;
			}
		}
		return check;
	}

	int loopChecker(List<ErrorAction> ea) {
		List<Error> nums = new ArrayList<Error>();
		int value = 0;
		int index, index2 = 0;
		for (int i = 0; i < ea.size(); i++) {
			nums.add(ea.get(i).getError());
			if (nums.size() > 2) {
				if (ea.get(i).getError().getCode() == nums.get(i - 2).getCode()) {
					if (ea.get(i).getError().getWhere().get(0) == null) {
						index = 1;
					} else {
						index = 0;
					}
					if (nums.get(i - 2).getWhere().get(0) == null) {
						index2 = 1;
					} else {
						index2 = 0;
					}
					if (ea.get(i).getError().getWhere().get(index).getClass() == nums.get(i - 2).getWhere().get(index2)
							.getClass()) {
						value++;
					}
				}
			}
		}
		return value;
	}

	Sequence bestSequence(List<Sequence> sm) {
		double max = -1;
		rewardSmallorBig(sm);
		Sequence maxS = new Sequence();
		for (Sequence s : sm) {
			// normalize weights so that longer rewards dont get priority
			if (s.getWeight() > max) {
				max = s.getWeight();
				maxS = s;
			}
		}
		return maxS;
	}

	// Action rewards
	int rewardCalculator(Error state, Action action) {
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

	void addTagMap(Error state, int num, Action action, int tag, int r) {
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

	public static void createTags(int user) {
		switch (user) {
		// ECMFA paper preferences
		case 0:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 2, 4 }));
			break;
		// error hierarchy high, sequence short
		case 1:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 0, 2 }));
			break;
		// error hierarchy low, sequence long, high modification
		case 2:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 1, 3, 6 }));
			break;
		// avoid deletion
		case 3:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 4 }));
			break;
		// short sequence, low modification
		case 4:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 0, 4, 5 }));
			break;
		// long sequence, low modification, avoid deletion
		case 5:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 1, 5 }));
			break;
		// error hierarchy high
		case 6:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 2 }));
			break;
		}
	}

//	/**
//	 * Gets the knowledge
//	 * 
//	 * @return the knowledge
//	 */
//	public Knowledge getKnowledge() {
//		return knowledge;
//	}
}
