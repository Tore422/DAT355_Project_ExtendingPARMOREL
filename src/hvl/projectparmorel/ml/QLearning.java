package hvl.projectparmorel.ml;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.reward.RewardCalculator;

/**
 * Western Norway University of Applied Sciences Bergen, Norway
 * 
 * @author Angela Barriga Rodriguez abar@hvl.no
 * @author Magnus Marthinsen
 */
public class QLearning {
	private hvl.projectparmorel.knowledge.Knowledge knowledge;
	private QTable qTable;
	private ActionExtractor actionExtractor;
	
	private ModelProcesser modelProcesser;
	
	protected static int N_EPISODES = 25;
	protected static double randomfactor = 0.25;

	private List<Error> errorsToFix;

	private Logger logger = Logger.getGlobal();

	private final double MIN_ALPHA = 0.06; // Learning rate
	private final double gamma = 1.0; // Eagerness - 0 looks in the near future, 1 looks in the distant future
	private int reward = 0;
	public URI uri;
	List<Error> original = new ArrayList<Error>();
	public List<Integer> originalCodes = new ArrayList<Integer>();
	List<Sequence> solvingMap = new ArrayList<Sequence>();
	public ResourceSet resourceSet = new ResourceSetImpl();

	int MAX_EPISODE_STEPS = 20;
	public Resource myMetaModel;
	public static int user;

	Sequence sx;

	private RewardCalculator rewardCalculator;

	public QLearning(List<Integer> preferences) {
		errorsToFix = new ArrayList<Error>();
		knowledge = new hvl.projectparmorel.knowledge.Knowledge();
		qTable = knowledge.getQTable();
		actionExtractor = new ActionExtractor(knowledge);
		rewardCalculator = new RewardCalculator(knowledge, preferences);
		modelProcesser = new ModelProcesser(resourceSet, knowledge, rewardCalculator);
	}
	
	public QLearning() {
		errorsToFix = new ArrayList<Error>();
		knowledge = new hvl.projectparmorel.knowledge.Knowledge();
	}

	public List<Integer> getPreferences(){
		return rewardCalculator.getPreferences();
	}
	
	public void setPreferences(List<Integer> preferences) {
		rewardCalculator = new RewardCalculator(knowledge, preferences);
	}
	
//	/**
//	 * Saves the knowledge
//	 */
//	public void saveKnowledge() {
//		knowledge.save();
//	}

	private static double[] linspace(double min, double max, int points) {
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
		int val;
		int discarded = 0;
		int episode = 0;
		boolean nope = false;

		Resource modelCopy = copy(myMetaModel, uri);
		errorsToFix = ErrorExtractor.extractErrorsFrom(modelCopy);
		solvingMap.clear();
		original.clear();
		original.addAll(errorsToFix);

		// FILTER ACTIONS AND INITIALICES QTABLE
		
		modelProcesser.initializeQTableForErrorsInModel(modelCopy, uri);
		// START with initial model its errors and actions
		logger.info("Errors to fix: " + errorsToFix.toString());
		logger.info("Number of episodes: " + N_EPISODES);
		while (episode < N_EPISODES) {
			int index = 0;
			int total_reward = 0;
			int step = 0;
			Sequence s = new Sequence();
			while (step < MAX_EPISODE_STEPS) {
				if(errorsToFix.size() != 0) {
					Error currentErrorToFix = errorsToFix.get(index);
					total_reward += handleStep(modelCopy, s, episode, currentErrorToFix);
					step++;
				} else {
					break;
				}
			}
			// add the whole sequence into list

				try {
					s.setModel(modelCopy);
				} catch (java.lang.NullPointerException exception) {
					// Catch NullPointerExceptions.
					nope = true;
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
		}
		setBestSeq(bestSequence(solvingMap));

		logger.info("\n-----------------ALL SEQUENCES FOUND-------------------" + "\nSIZE: " + solvingMap.size()
				+ "\nDISCARDED SEQUENCES: " + discarded + "\n--------::::B E S T   S E Q U E N C E   I S::::---------\n"
				+ getBestSeq().toString());

		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// THIS SAVES THE REPAIRED MODEL
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if (getBestSeq().getSeq().size() != 0) {
			rewardCalculator.rewardSequence(getBestSeq(), -1);
			sx.getModel().save(null);
		}
	}

	private int handleStep(Resource modelCopy, Sequence s, int episode, Error currentErrorToFix) {
		Action action = chooseAction(currentErrorToFix);
		int sizeBefore = errorsToFix.size();
		double alpha = alphas[episode];

		errorsToFix.clear();
		errorsToFix = modelProcesser.tryApplyAction(currentErrorToFix, action, modelCopy,
				action.getHierarchy()); // removed subHirerarchy - effect?
		reward = rewardCalculator.calculateRewardFor(currentErrorToFix, action);
		// Insert stuff into sequence
		s.setId(episode);
		List<ErrorAction> ea = s.getSeq();
		ea.add(new ErrorAction(currentErrorToFix, action));
//		if ((currentErrorToFix.getCode() == 401 || currentErrorToFix.getCode() == 445
//				|| currentErrorToFix.getCode() == 27 || currentErrorToFix.getCode() == 32)
//				&& (action.getMsg().contentEquals("setEType") || action.getMsg().contentEquals("delete")
//						|| action.getMsg().contentEquals("setName")
//						|| action.getMsg().contentEquals("unsetEGenericType"))) {
//			// alert
//		}
		s.setSeq(ea);
		s.setU(uri);

		int code;
		if (action.getSubHierarchy() != -1) {
			code = Integer
					.valueOf(String.valueOf(action.getHierarchy()) + String.valueOf(action.getSubHierarchy()));
		} else {
			code = action.getHierarchy();
		}

		reward = rewardCalculator.updateBasedOnNumberOfErrors(reward, sizeBefore, errorsToFix.size(),
				currentErrorToFix, code, action);

		if (errorsToFix.size() != 0) {
			Error nextErrorToFix = errorsToFix.get(0);

			if (!qTable.containsErrorCode(nextErrorToFix.getCode())) {
				errorsToFix = ErrorExtractor.extractErrorsFrom(modelCopy);
				actionExtractor.extractActionsFor(errorsToFix);
				modelProcesser.initializeQTableForErrorsInModel(modelCopy, uri);
			}

			reward = rewardCalculator.updateIfNewErrorIsIntroduced(reward, originalCodes, nextErrorToFix);

			nextErrorToFix = errorsToFix.get(0);
			Action a = knowledge.getOptimalActionForErrorCode(nextErrorToFix.getCode());

			int code2;
			if (a.getSubHierarchy() != -1) {
				code2 = Integer.valueOf(String.valueOf(a.getHierarchy()) + String.valueOf(a.getSubHierarchy()));
			} else {
				code2 = a.getHierarchy();
			}
			double value = qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode())
					+ alpha * (reward + gamma * qTable.getWeight(nextErrorToFix.getCode(), code2, a.getCode()))
					- qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode());

			qTable.setWeight(currentErrorToFix.getCode(), code, action.getCode(), value);
			currentErrorToFix = nextErrorToFix;
		} // it has reached the end

		else {
			double value = qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode())
					+ alpha * (reward + gamma)
					- qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode());

			qTable.setWeight(currentErrorToFix.getCode(), code, action.getCode(), value);
		}

		return reward;
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
		rewardCalculator.rewardBasedOnSequenceLength(sm);
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

//	/**
//	 * Gets the knowledge
//	 * 
//	 * @return the knowledge
//	 */
//	public Knowledge getKnowledge() {
//		return knowledge;
//	}
}
