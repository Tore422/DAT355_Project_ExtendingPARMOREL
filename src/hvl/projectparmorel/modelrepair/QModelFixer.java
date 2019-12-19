package hvl.projectparmorel.modelrepair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import hvl.projectparmorel.ecore.EcoreActionExtractor;
import hvl.projectparmorel.ecore.EcoreErrorExtractor;
import hvl.projectparmorel.ecore.EcoreModel;
import hvl.projectparmorel.ecore.EcoreModelProcessor;
import hvl.projectparmorel.exceptions.UnsupportedErrorException;
import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.ActionExtractor;
import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.ErrorExtractor;
import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.general.ModelFixer;
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
	private ErrorExtractor errorExtractor;
	private EcoreModelProcessor modelProcesser;

	private double randomFactor = 0.25;
	private int numberOfEpisodes = 25;

	private List<Error> errorsToFix;
	private int discardedSequences;

	private Logger logger = Logger.getGlobal();

	private int reward = 0;
	private File originalModel;
	private URI uri;
	private List<Error> originalErrors;
	private List<Integer> initialErrorCodes;
	private List<Solution> possibleSolutions;
	private ResourceSet resourceSet;
	private RewardCalculator rewardCalculator;
	
	private Set<Integer> unsupportedErrorCodes;

	public QModelFixer() {
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		errorsToFix = new ArrayList<Error>();
		knowledge = new Knowledge();
		qTable = knowledge.getQTable();
		actionExtractor = new EcoreActionExtractor(knowledge);
		unsupportedErrorCodes = new HashSet<>();
		errorExtractor = new EcoreErrorExtractor(unsupportedErrorCodes);
		discardedSequences = 0;
		originalErrors = new ArrayList<Error>();
		initialErrorCodes = new ArrayList<Integer>();
		possibleSolutions = new ArrayList<Solution>();
		rewardCalculator = new RewardCalculator(knowledge, new ArrayList<>());
		modelProcesser = new EcoreModelProcessor(knowledge, rewardCalculator, unsupportedErrorCodes);
		ALPHAS = linspace(1.0, MIN_ALPHA, numberOfEpisodes);
		
		unsupportedErrorCodes.add(4);
		unsupportedErrorCodes.add(6);
		loadKnowledge();
	}

	public QModelFixer(List<Integer> preferences) {
		this();
		rewardCalculator = new RewardCalculator(knowledge, preferences);
		modelProcesser = new EcoreModelProcessor(knowledge, rewardCalculator, unsupportedErrorCodes);
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

	@Override
	public void setPreferences(List<Integer> preferences) {
		rewardCalculator = new RewardCalculator(knowledge, preferences);
		modelProcesser = new EcoreModelProcessor(knowledge, rewardCalculator, unsupportedErrorCodes);
	}

	/**
	 * Saves the knowledge
	 */
	private void saveKnowledge() {
		knowledge.save();
	}

	/**
	 * Loads knowledge from file and influences the weights in the q-table from this
	 * based on preferences. Only 20 % of the value from before is loaded. This
	 * allows for new learnings to be acquired.
	 * 
	 * This also reduces the number of episodes and the chance for taking random
	 * actions. More previous knowledge reduces the need for many episodes and
	 * randomness.
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
	 * @throws UnsupportedErrorException if the error is not in the Q-table
	 */
	private Action chooseAction(Error error) throws UnsupportedErrorException {
		if (Math.random() < randomFactor) {
			logger.info("Choosing random action.");
			return knowledge.getQTable().getRandomActionForError(error.getCode());
		} else {
			logger.info("Choosing optimal action");
			return knowledge.getOptimalActionForErrorCode(error.getCode());
		}
	}

	@Override
	public Solution fixModel(File modelFile) {
		originalModel = modelFile;
		File duplicateFile = createDuplicateFile();
		this.uri = URI.createFileURI(duplicateFile.getAbsolutePath());
		Resource modelResource = getModel(uri);

		logger.info("Running with preferences " + rewardCalculator.getPreferences().toString());

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		discardedSequences = 0;
		int episode = 0;
		
		Model model = new EcoreModel(resourceSet, modelResource, uri);

//		Resource modelCopy = copy(modelResource, uri);
		errorsToFix = errorExtractor.extractErrorsFrom(model.getRepresentationCopy());
		setInitialErrors(errorsToFix);
		possibleSolutions.clear();
		originalErrors.clear();
		originalErrors.addAll(errorsToFix);

//		Model modelCopy = new EcoreModel(resourceSet, modelCopy, uri);
		modelProcesser.initializeQTableForErrorsInModel(model);

		logger.info("Errors to fix: " + errorsToFix.toString());
		logger.info("Number of episodes: " + numberOfEpisodes);
		while (episode < numberOfEpisodes) {
			File episodeModel = createDuplicateFile(duplicateFile, originalModel.getParent()
					+ "parmorel_temp_solution_" + episode + "_" + originalModel.getName());
			
			URI episodeModelUri = URI.createFileURI(episodeModel.getAbsolutePath());
			Resource episodeModelResource = getModel(episodeModelUri);
			
			Solution sequence = handleEpisode(episodeModelResource, episode);

			if (sequence != null) {
				episodeModel.deleteOnExit();
				try {
					episodeModelResource.save(null);
					sequence.setModel(episodeModel);
				} catch (NullPointerException exception) {
					exception.printStackTrace();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			} else {
				episodeModel.delete();
			}
			

			// RESET initial model and extract actions + errors
//			modelCopy.getContents().clear();
//			EObject content = modelResource.getContents().get(0);
//			modelCopy.getContents().add(EcoreUtil.copy(content));
			errorsToFix.clear();
			errorsToFix.addAll(originalErrors);
			episode++;
		}
		Solution bestSequence = bestSequence(possibleSolutions);
		duplicateFile.delete();

		logger.info("\n-----------------ALL SEQUENCES FOUND-------------------" + "\nSIZE: " + possibleSolutions.size()
				+ "\nDISCARDED SEQUENCES: " + discardedSequences
				+ "\n--------::::B E S T   S E Q U E N C E   I S::::---------\n" + bestSequence);

//		removeSolutionsWithSameResult(solvingMap);

		if (bestSequence.getSequence().size() != 0) {
			rewardCalculator.rewardSequence(bestSequence, -1);
		}
		saveKnowledge();
		return bestSequence;
	}

	/**
	 * Takes the original file as parameter and creates a duplicate of the file that
	 * will represent the repaired model.
	 * 
	 * @param file to copy
	 * @param path to the new copy
	 * @return the created duplicate
	 */
	private File createDuplicateFile(File fileToCopy, String path) {
		File destinationFile = new File(path);
		try {
			Files.copy(fileToCopy.toPath(), destinationFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return destinationFile;
	}

	/**
	 * Takes the original model-file and creates a duplicate of the file that will
	 * represent the repaired model.
	 * 
	 * @return the created duplicate
	 */
	private File createDuplicateFile() {
		return createDuplicateFile(originalModel, originalModel.getParent() + "_temp_" + originalModel.getName());
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
	private Solution handleEpisode(Resource modelCopy, int episode) {
		Solution sequence = new Solution();
		boolean errorOcurred = false;
		int totalReward = 0;
		int step = 0;

		while (step < MAX_EPISODE_STEPS) {
			while (!errorsToFix.isEmpty()
					&& unsupportedErrorCodes.contains(errorsToFix.get(0).getCode())) {
				logger.warning("UNSUPORTED ERROR CODE: " + errorsToFix.get(0).getCode() + "\nMessage: "
						+ errorsToFix.get(0).getMessage());
				errorsToFix.remove(0);
			}
			if (!errorsToFix.isEmpty()) {
				Error currentErrorToFix = errorsToFix.get(0);
				try {
					totalReward += handleStep(modelCopy, sequence, episode, currentErrorToFix);
				} catch (UnsupportedErrorException e) {
					logger.warning("Encountered error that could not be resolved. + \nCode:"
							+ currentErrorToFix.getCode() + "\nMessage:" + currentErrorToFix.getMessage());
					errorsToFix.remove(0);
				}
				step++;
			} else {
				break;
			}
		}

		int val;
		if (sequence.getSequence().size() > 7) {
			val = checkForLoopsIn(sequence.getSequence());
			if (val > 1) {
				totalReward -= val * 1000;
			}
		}

		sequence.setWeight(totalReward);
		sequence.setOriginal(originalModel);

		if (!errorOcurred && uniqueSequence(sequence)) {
			possibleSolutions.add(sequence);
		} else {
			discardedSequences++;
			sequence = null;
		}

		logger.info("EPISODE " + episode + " TOTAL REWARD " + totalReward);
		return sequence;
	}

	/**
	 * Handles a single step
	 * 
	 * @param modelCopy
	 * @param sequence
	 * @param episode
	 * @param currentErrorToFix
	 * @return the reward from the step
	 * @throws UnsupportedErrorException if the error code is not in the Q-table,
	 *                                   and cannot be added
	 */
	private int handleStep(Resource modelCopy, Solution sequence, int episode, Error currentErrorToFix)
			throws UnsupportedErrorException {
		logger.info("Fixing error " + currentErrorToFix.getCode() + " int episode " + episode);
		if (!qTable.containsErrorCode(currentErrorToFix.getCode())) {
			logger.info(
					"Error code " + currentErrorToFix.getCode() + " does not exist in Q-table, attempting to solve...");
			errorsToFix = errorExtractor.extractErrorsFrom(modelCopy);
			actionExtractor.extractActionsFor(errorsToFix);
			Model model = new EcoreModel(resourceSet, modelCopy, uri);
			modelProcesser.initializeQTableForErrorsInModel(model);
			if (!qTable.containsErrorCode(currentErrorToFix.getCode())) {
				logger.info("Action for error code not found.");
			} else {
				logger.info("Action for error code found and added to Q-table.");
			}
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

		int code = action.getHierarchy();

		reward = rewardCalculator.updateBasedOnNumberOfErrors(reward, sizeBefore, errorsToFix.size(), currentErrorToFix,
				code, action);

		if (!errorsToFix.isEmpty()) {
			Error nextErrorToFix = errorsToFix.get(0);
			logger.info("Next error code: " + nextErrorToFix.getCode());
			if (!qTable.containsErrorCode(nextErrorToFix.getCode())) {
				logger.info("Error code " + currentErrorToFix.getCode()
						+ " does not exist in Q-table, attempting to solve...");
				errorsToFix = errorExtractor.extractErrorsFrom(modelCopy);
				actionExtractor.extractActionsFor(errorsToFix);
				Model model = new EcoreModel(resourceSet, modelCopy, uri);
				modelProcesser.initializeQTableForErrorsInModel(model);
				if (!qTable.containsErrorCode(nextErrorToFix.getCode())) {
					logger.info("Action for error code not found.");
				} else {
					logger.info("Action for error code found and added to Q-table.");
				}
			}

			reward = rewardCalculator.updateIfNewErrorIsIntroduced(reward, initialErrorCodes, nextErrorToFix);

			nextErrorToFix = errorsToFix.get(0);
			Action a;
			try {
				a = knowledge.getOptimalActionForErrorCode(nextErrorToFix.getCode());
				int code2 = a.getHierarchy();
				double value = qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode())
						+ alpha * (reward + GAMMA * qTable.getWeight(nextErrorToFix.getCode(), code2, a.getCode()))
						- qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode());

				qTable.setWeight(currentErrorToFix.getCode(), code, action.getCode(), value);
			} catch (UnsupportedErrorException e) {
				// next error is not in the Q-table
			}

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
	@Override
	public Resource copy(Resource model, URI uri) {
		Resource modelCopy = resourceSet.createResource(uri);
//		modelCopy.getContents().add(EcoreUtil.copy(model.getContents().get(0)));
		EList<EObject> contents = model.getContents();
		modelCopy.getContents().addAll(EcoreUtil.copyAll(contents));
		return modelCopy;
	}

	private boolean uniqueSequence(Solution sequence) {
		boolean check = true;
		int same = 0;
		for (Solution otherSequence : possibleSolutions) {
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
					if (performedActions.get(i).getError().getContexts().get(index).getClass() == errors.get(i - 2)
							.getContexts().get(index2).getClass()) {
						value++;
					}
				}
			}
		}
		return value;
	}

	private Solution bestSequence(List<Solution> sm) {
		double max = -1;
		rewardCalculator.rewardBasedOnSequenceLength(sm);
		Solution maxS = new Solution();
		for (Solution s : sm) {
			// normalize weights so that longer rewards dont get priority
			if (s.getWeight() > max) {
				max = s.getWeight();
				maxS = s;
			}
		}
		return maxS;
	}

//	private void removeSolutionsWithSameResult(List<Solution> possibleSolutions) {
//		for(int i = 0; i < possibleSolutions.size(); i++) {
//			for(int j = 1; j < possibleSolutions.size(); j++) {
//				if(i != j) {
//					Solution solution1 = possibleSolutions.get(i);
//					Solution solution2 = possibleSolutions.get(i);
//					URI uri1 = URI.createFileURI(solution1.getModel().getAbsolutePath());
//					URI uri2 = URI.createFileURI(solution2.getModel().getAbsolutePath());
//					Resource model1 = getModel(uri1);
//					Resource model2 = getModel(uri2);
////					org.eclipse.emf.ecore.util.EcoreUtil.equals(model1, model2);
//					
//				}
//			}
//		}
//		
//	}

	@Override
	public Resource getModel(URI uri) {
		return resourceSet.getResource(uri, true);
	}

	@Override
	public boolean modelIsBroken(Resource model) {
		List<Error> errors = errorExtractor.extractErrorsFrom(model);
		return !errors.isEmpty();
	}

	@Override
	public List<Solution> getPossibleSolutions() {
		List<Solution> solutions = new ArrayList<>(possibleSolutions);
		solutions.sort(Collections.reverseOrder());
		return solutions;
	}
}