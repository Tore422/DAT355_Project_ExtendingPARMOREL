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
import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.reward.RewardCalculator;

/**
 * A model fixer that uses QLearning.
 * 
 * Western Norway University of Applied Sciences Bergen, Norway
 * 
 * @author Angela Barriga Rodriguez abar@hvl.no
 * @author Magnus Marthinsen
 */
public class QModelFixer implements ModelFixer {
	private final double MIN_ALPHA = 0.06; // Learning rate
	private final double GAMMA = 1.0; // Eagerness - 0 looks in the near future, 1 looks in the distant future
	private final int MAX_EPISODE_STEPS = 20;
	private final double[] ALPHAS;

	private Knowledge knowledge;
	private QTable qTable;
	private ActionExtractor actionExtractor;
	private ModelProcesser modelProcesser;

	private double randomFactor = 0.25;
	private int numberOfEpisodes = 25;

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

	public QModelFixer() {
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		errorsToFix = new ArrayList<Error>();
		knowledge = new Knowledge();
		qTable = knowledge.getQTable();
		actionExtractor = new ActionExtractor(knowledge);
		discardedSequences = 0;
		originalErrors = new ArrayList<Error>();
		initialErrorCodes = new ArrayList<Integer>();
		solvingMap = new ArrayList<Sequence>();
		rewardCalculator = new RewardCalculator(knowledge, new ArrayList<>());
		modelProcesser = new ModelProcesser(resourceSet, knowledge, rewardCalculator);
		ALPHAS = linspace(1.0, MIN_ALPHA, numberOfEpisodes);
		loadKnowledge();
	}

	public QModelFixer(List<Integer> preferences) {
		this();
		rewardCalculator = new RewardCalculator(knowledge, preferences);
		modelProcesser = new ModelProcesser(resourceSet, knowledge, rewardCalculator);
	}
	
	private double[] linspace(double min, double max, int points) {
		double[] d = new double[points];
		for (int i = 0; i < points; i++) {
			d[i] = min + i * (max - min) / (points - 1);
		}
		return d;
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
	private void saveKnowledge() {
		knowledge.save();
	}

	/**
	 * Loads knowledge from file and influences the weights in the q-table from this
	 * based on preferences. Only 20 % of the value from before is loaded. This allows for new learnings to be aqcuired.
	 * 
	 * This also reduces the number of episodes and the chance
	 * for taking random actions. More previous knowledge reduces the need for many episodes and randomness.
	 */
	private void loadKnowledge() {
		boolean success = knowledge.load();
		if (success) {
			knowledge.clearWeights();
			rewardCalculator.influenceWeightsFromPreferencesBy(0.2);
			numberOfEpisodes = 12;
			randomFactor = 0.15;
		}
	}	

	/**
	 * Chooses an action for the specified error. The action is either the best
	 * action based on the previous knowledge, or a random action.
	 * 
	 * @param error
	 * @return a fitting action
	 */
	private Action chooseAction(Error error) {
		if (Math.random() < randomFactor) {
			return knowledge.getQTable().getRandomActionForError(error.getCode());
		} else {
			return knowledge.getOptimalActionForErrorCode(error.getCode());
		}
	}

	@Override
	public Sequence fixModel(Resource model, URI uri) {
		logger.info("Running with preferences " + getPreferences().toString());
		
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

		modelProcesser.initializeQTableForErrorsInModel(modelCopy, uri);


		logger.info("Errors to fix: " + errorsToFix.toString());
		logger.info("Number of episodes: " + numberOfEpisodes);
		while (episode < numberOfEpisodes) {
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
				+ "\nDISCARDED SEQUENCES: " + discardedSequences
				+ "\n--------::::B E S T   S E Q U E N C E   I S::::---------\n" + bestSequence);

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
		saveKnowledge();
		return bestSequence;
	}

	/**
	 * Sets the initial error codes
	 * 
	 * @param errors
	 */
	private void setInitialErrors(List<Error> errors) {
		for (Error error : errors) {
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
			if (!errorsToFix.isEmpty()) {
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
			val = checkForLoopsIn(sequence.getSequence());
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
		if (!qTable.containsErrorCode(currentErrorToFix.getCode())) {
			errorsToFix = ErrorExtractor.extractErrorsFrom(modelCopy);
			actionExtractor.extractActionsFor(errorsToFix);
			modelProcesser.initializeQTableForErrorsInModel(modelCopy, uri);
		}
		
		Action action = chooseAction(currentErrorToFix);
		int sizeBefore = errorsToFix.size();
		double alpha = ALPHAS[episode];

		errorsToFix.clear();
		errorsToFix = modelProcesser.tryApplyAction(currentErrorToFix, action, modelCopy, action.getHierarchy());
		reward = rewardCalculator.calculateRewardFor(currentErrorToFix, action);
		// Insert stuff into sequence
		sequence.setId(episode);
		List<AppliedAction> appliedActions = sequence.getSequence();
		appliedActions.add(new AppliedAction(currentErrorToFix, action));

		sequence.setSequence(appliedActions);
		sequence.setURI(uri);

		int code = action.getHierarchy();

		reward = rewardCalculator.updateBasedOnNumberOfErrors(reward, sizeBefore, errorsToFix.size(), currentErrorToFix,
				code, action);

		if (!errorsToFix.isEmpty()) {
			Error nextErrorToFix = errorsToFix.get(0);

			if (!qTable.containsErrorCode(nextErrorToFix.getCode())) {
				errorsToFix = ErrorExtractor.extractErrorsFrom(modelCopy);
				actionExtractor.extractActionsFor(errorsToFix);
				modelProcesser.initializeQTableForErrorsInModel(modelCopy, uri);
			}

			reward = rewardCalculator.updateIfNewErrorIsIntroduced(reward, initialErrorCodes, nextErrorToFix);

			nextErrorToFix = errorsToFix.get(0);
			Action a = knowledge.getOptimalActionForErrorCode(nextErrorToFix.getCode());

			int code2 = a.getHierarchy();
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

	private boolean uniqueSequence(Sequence sequence) {
		boolean check = true;
		int same = 0;
		for (Sequence otherSequence : solvingMap) {
			if (otherSequence.getWeight() == sequence.getWeight()) {
				for (AppliedAction ea : sequence.getSequence()) {
					for (AppliedAction ea2 : otherSequence.getSequence()) {
						if (ea.equals(ea2)) {
							same++;
						}
					}
				} // for ea
					// if all elements in list are the same
				if (same == sequence.getSequence().size()) {
					check = false;
					break;
				}
			} // if weight
		} // for
		return check;
	}

	private int checkForLoopsIn(List<AppliedAction> performedActions) {
		List<Error> errors = new ArrayList<Error>();
		int value = 0;
		int index, index2 = 0;
		for (int i = 0; i < performedActions.size(); i++) {
			errors.add(performedActions.get(i).getError());
			if (errors.size() > 2) {
				if (performedActions.get(i).getError().getCode() == errors.get(i - 2).getCode()) {
					if (performedActions.get(i).getError().getContexts().get(0) == null) {
						index = 1;
					} else {
						index = 0;
					}
					if (errors.get(i - 2).getContexts().get(0) == null) {
						index2 = 1;
					} else {
						index2 = 0;
					}
					if (performedActions.get(i).getError().getContexts().get(index).getClass() == errors.get(i - 2).getContexts()
							.get(index2).getClass()) {
						value++;
					}
				}
			}
		}
		return value;
	}

	private Sequence bestSequence(List<Sequence> sm) {
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

	@Override
	public Resource getModel(URI uri) {
		return resourceSet.getResource(uri, true);
	}

	@Override
	public boolean modelIsBroken(Resource model) {
		List<Error> errors = ErrorExtractor.extractErrorsFrom(model);
		return !errors.isEmpty();
	}

}