package hvl.projectparmorel.ml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import hvl.projectparmorel.knowledge.Action;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.reward.RewardCalculator;

/**
 * Western Norway University of Applied Sciences Bergen, Norway
 * 
 * @author Angela Barriga Rodriguez abar@hvl.no
 * @author Magnus Marthinsen
 */
public class QLearning {
	private final double MIN_ALPHA = 0.06; // Learning rate
	private final double GAMMA = 1.0; // Eagerness - 0 looks in the near future, 1 looks in the distant future
	private final int NUMBER_OF_EPISODES = 25;
	private final int MAX_EPISODE_STEPS = 20;
	
	private Knowledge knowledge;
	private QTable qTable;
	private ActionExtractor actionExtractor;
	private ModelProcesser modelProcesser;

	private double randomfactor = 0.25;

	private List<Error> errorsToFix;
	private int discardedSequences;

	private Logger logger = Logger.getGlobal();

	private int reward = 0;
	private URI uri;
	private List<Error> originalErrors;
	private List<Integer> initialErrorCodes;
	private List<Sequence> solvingMap;
	private ResourceSet resourceSet;
	private RewardCalculator rewardCalculator;

	public QLearning() {
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		errorsToFix = new ArrayList<Error>();
		knowledge = new Knowledge();
		qTable = knowledge.getActionDirectory();
		actionExtractor = new ActionExtractor(knowledge);
		discardedSequences = 0;
		originalErrors = new ArrayList<Error>();
		initialErrorCodes = new ArrayList<Integer>();
		solvingMap = new ArrayList<Sequence>();
	}
	
	public QLearning(List<Integer> preferences) {
		this();
		rewardCalculator = new RewardCalculator(knowledge, preferences);
		modelProcesser = new ModelProcesser(resourceSet, knowledge, rewardCalculator);
	}
	
	public ResourceSet getResourceSet() {
		return resourceSet;
	}

	public List<Integer> getPreferences() {
		return rewardCalculator.getPreferences();
	}

	public void setPreferences(List<Integer> preferences) {
		rewardCalculator = new RewardCalculator(knowledge, preferences);
		modelProcesser = new ModelProcesser(resourceSet, knowledge, rewardCalculator);
	}

	/**
	 * Saves the knowledge
	 */
	public void saveKnowledge() {
		knowledge.save();
	}

	/**
	 * Loads knowledge from file
	 */
	public void loadKnowledge() {
		knowledge.load();
	}
	
	private static double[] linspace(double min, double max, int points) {
		double[] d = new double[points];
		for (int i = 0; i < points; i++) {
			d[i] = min + i * (max - min) / (points - 1);
		}
		return d;
	}

	double[] alphas = linspace(1.0, MIN_ALPHA, NUMBER_OF_EPISODES);

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

	/**
	 * Attempts to fix the model
	 * 
	 * @param model
	 * @param uri
	 * @return the best possible sequence
	 */
	public Sequence fixModel(Resource model, URI uri) {
		this.uri = uri;
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		discardedSequences = 0;
		int episode = 0;

		Resource modelCopy = copy(model, uri);
		errorsToFix = ErrorExtractor.extractErrorsFrom(modelCopy);
		setInitialErrors(errorsToFix);
		solvingMap.clear();
		originalErrors.clear();
		originalErrors.addAll(errorsToFix);

		// FILTER ACTIONS AND INITIALICES QTABLE

		modelProcesser.initializeQTableForErrorsInModel(modelCopy, uri);
		// START with initial model its errors and actions
		logger.info("Errors to fix: " + errorsToFix.toString());
		logger.info("Number of episodes: " + NUMBER_OF_EPISODES);
		while (episode < NUMBER_OF_EPISODES) {
			handleEpisode(modelCopy, episode);
			
			// RESET initial model and extract actions + errors
			modelCopy.getContents().clear();
			modelCopy.getContents().add(EcoreUtil.copy(model.getContents().get(0)));
			errorsToFix.clear();
			errorsToFix.addAll(originalErrors);
			episode++;
		}
		Sequence bestSequence = bestSequence(solvingMap);

		logger.info("\n-----------------ALL SEQUENCES FOUND-------------------" + "\nSIZE: " + solvingMap.size()
				+ "\nDISCARDED SEQUENCES: " + discardedSequences + "\n--------::::B E S T   S E Q U E N C E   I S::::---------\n"
				+ bestSequence);

		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// THIS SAVES THE REPAIRED MODEL
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if (bestSequence.getSequence().size() != 0) {
			rewardCalculator.rewardSequence(bestSequence, -1);
			try {
				bestSequence.getModel().save(null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bestSequence;
	}
	
	/**
	 * Sets the initial error codes
	 * 
	 * @param errors
	 */
	private void setInitialErrors(List<Error> errors) {
		for(Error error : errors) {
			initialErrorCodes.add(error.getCode());
		}
	}

	/**
	 * Handles a single episode
	 * 
	 * @param modelCopy
	 * @param episode
	 */
	private void handleEpisode(Resource modelCopy, int episode) {
		Sequence sequence = new Sequence();
		boolean errorOcurred = false;
		int totalReward = 0;
		int step = 0;
		
		while (step < MAX_EPISODE_STEPS) {
			if (errorsToFix.size() != 0) {
				Error currentErrorToFix = errorsToFix.get(0);
				totalReward += handleStep(modelCopy, sequence, episode, currentErrorToFix);
				
				step++;
			} else {
				break;
			}
		}

		try {
			sequence.setModel(modelCopy);
		} catch (NullPointerException exception) {
			errorOcurred = true;
		}

		int val;
		if (sequence.getSequence().size() > 7) {
			val = loopChecker(sequence.getSequence());
			if (val > 1) {
				totalReward -= val * 1000;
			}
		}

		sequence.setWeight(totalReward);

		if (!errorOcurred && uniqueSequence(sequence)) {
			solvingMap.add(sequence);
		} else {
			discardedSequences++;
		}

		logger.info("EPISODE " + episode + " TOTAL REWARD " + totalReward);	
	}

	/**
	 * Handles a single step
	 * 
	 * @param modelCopy
	 * @param sequence
	 * @param episode
	 * @param currentErrorToFix
	 * @return the reward from the step
	 */
	private int handleStep(Resource modelCopy, Sequence sequence, int episode, Error currentErrorToFix) {
		Action action = chooseAction(currentErrorToFix);
		int sizeBefore = errorsToFix.size();
		double alpha = alphas[episode];

		errorsToFix.clear();
		errorsToFix = modelProcesser.tryApplyAction(currentErrorToFix, action, modelCopy, action.getHierarchy()); // removed
																													// subHirerarchy
																													// -
																													// effect?
		reward = rewardCalculator.calculateRewardFor(currentErrorToFix, action);
		// Insert stuff into sequence
		sequence.setId(episode);
		List<ErrorAction> errorActionList = sequence.getSequence();
		errorActionList.add(new ErrorAction(currentErrorToFix, action));

		sequence.setSequence(errorActionList);
		sequence.setURI(uri);

		int code;
		if (action.getSubHierarchy() != -1) {
			code = Integer.valueOf(String.valueOf(action.getHierarchy()) + String.valueOf(action.getSubHierarchy()));
		} else {
			code = action.getHierarchy();
		}

		reward = rewardCalculator.updateBasedOnNumberOfErrors(reward, sizeBefore, errorsToFix.size(), currentErrorToFix,
				code, action);

		if (errorsToFix.size() != 0) {
			Error nextErrorToFix = errorsToFix.get(0);

			if (!qTable.containsErrorCode(nextErrorToFix.getCode())) {
				errorsToFix = ErrorExtractor.extractErrorsFrom(modelCopy);
				actionExtractor.extractActionsFor(errorsToFix);
				modelProcesser.initializeQTableForErrorsInModel(modelCopy, uri);
			}

			reward = rewardCalculator.updateIfNewErrorIsIntroduced(reward, initialErrorCodes, nextErrorToFix);

			nextErrorToFix = errorsToFix.get(0);
			Action a = knowledge.getOptimalActionForErrorCode(nextErrorToFix.getCode());

			int code2;
			if (a.getSubHierarchy() != -1) {
				code2 = Integer.valueOf(String.valueOf(a.getHierarchy()) + String.valueOf(a.getSubHierarchy()));
			} else {
				code2 = a.getHierarchy();
			}
			double value = qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode())
					+ alpha * (reward + GAMMA * qTable.getWeight(nextErrorToFix.getCode(), code2, a.getCode()))
					- qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode());

			qTable.setWeight(currentErrorToFix.getCode(), code, action.getCode(), value);
			currentErrorToFix = nextErrorToFix;
		} // it has reached the end

		else {
			double value = qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode())
					+ alpha * (reward + GAMMA) - qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode());

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
				for (ErrorAction ea : s.getSequence()) {
					for (ErrorAction ea2 : seq.getSequence()) {
						if (ea.equals(ea2)) {
							same++;
						}
					}
				} // for ea
					// if all elements in list are the same
				if (same == s.getSequence().size()) {
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
					if (ea.get(i).getError().getContexts().get(0) == null) {
						index = 1;
					} else {
						index = 0;
					}
					if (nums.get(i - 2).getContexts().get(0) == null) {
						index2 = 1;
					} else {
						index2 = 0;
					}
					if (ea.get(i).getError().getContexts().get(index).getClass() == nums.get(i - 2).getContexts().get(index2)
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
