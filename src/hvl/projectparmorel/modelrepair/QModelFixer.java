package hvl.projectparmorel.modelrepair;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import hvl.projectparmorel.exceptions.NoErrorsInModelException;
import hvl.projectparmorel.exceptions.UnsupportedErrorException;
import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.ActionExtractor;
import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.ErrorExtractor;
import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.general.ModelFixer;
import hvl.projectparmorel.general.ModelProcessor;
import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.reward.PreferenceOption;
import hvl.projectparmorel.reward.RewardCalculator;

/**
 * A model fixer that uses QLearning.
 * 
 * Western Norway University of Applied Sciences Bergen, Norway
 * 
 * @author Angela Barriga Rodriguez abar@hvl.no
 * @author Magnus Marthinsen
 */
public abstract class QModelFixer implements ModelFixer {
	/**
	 * The name of the {@link java.util.logging.Logger} used.
	 */
	public static final String LOGGER_NAME = "MyLog";
	private final double MIN_ALPHA = 0.06; // Learning rate
	private final double GAMMA = 1.0; // Eagerness - 0 looks in the near future, 1 looks in the distant future
	private final int MIN_EPISODE_STEPS = 20;
	private final double[] ALPHAS;

	protected Knowledge knowledge;
	private QTable qTable;
	protected ActionExtractor actionExtractor;
	protected ErrorExtractor errorExtractor;
	protected ModelProcessor modelProcessor;
	protected RewardCalculator rewardCalculator;

	private double randomFactor = 0.25;
	private int numberOfEpisodes = 25;

	private List<Error> errorsToFix;
	private int discardedSequences;

	private Logger logger;

	private int reward = 0;
	private int numberOfSteps;
	protected File originalModel;
	private List<Error> originalErrors;
	private List<Integer> initialErrorCodes;
	private List<Solution> possibleSolutions;

	public QModelFixer() {
		errorsToFix = new ArrayList<Error>();
		knowledge = new Knowledge();
		qTable = knowledge.getQTable();
		discardedSequences = 0;
		originalErrors = new ArrayList<Error>();
		initialErrorCodes = new ArrayList<Integer>();
		possibleSolutions = new ArrayList<Solution>();
		rewardCalculator = new RewardCalculator(knowledge, new ArrayList<>());
		ALPHAS = linspace(1.0, MIN_ALPHA, numberOfEpisodes);
		numberOfSteps = MIN_EPISODE_STEPS;
		loadKnowledge();

		logger = Logger.getLogger(LOGGER_NAME);
	}

	public QModelFixer(List<PreferenceOption> preferences) {
		this();
		rewardCalculator = new RewardCalculator(knowledge, preferences);
	}

	private double[] linspace(double min, double max, int points) {
		double[] d = new double[points];
		for (int i = 0; i < points; i++) {
			d[i] = min + i * (max - min) / (points - 1);
		}
		return d;
	}

	@Override
	public void setPreferences(List<PreferenceOption> preferences) {
		rewardCalculator = new RewardCalculator(knowledge, preferences);
		updateRewardCalculator();
	}

	/**
	 * Updates the dependencies after reward calculator has changed.
	 */
	protected abstract void updateRewardCalculator();

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
	public Solution fixModel(File modelFile) throws NoErrorsInModelException {		
		long startTime = System.currentTimeMillis();
		logger.info("Repairing " + modelFile.getName());
		originalModel = modelFile;
		Model model = initializeModelFromFile();

		File duplicateFile = createDuplicateFile();

		logger.info("Running with preferences " + rewardCalculator.getPreferences().toString());

		discardedSequences = 0;
		int episode = 0;

		errorsToFix = errorExtractor.extractErrorsFrom(model.getRepresentationCopy(), true);
		handleUnsupportedErrors(model);
		if (errorsToFix.isEmpty()) {
			duplicateFile.delete();
			throw new NoErrorsInModelException("No supported errors where found in " + modelFile.getAbsolutePath());
		}
		
		setInitialErrors(errorsToFix);
		possibleSolutions.clear();
		originalErrors.clear();
		originalErrors.addAll(errorsToFix);
		if (errorsToFix.size() * 1.4 > MIN_EPISODE_STEPS) {
			numberOfSteps = (int) (errorsToFix.size() * 1.4);
		} else {
			numberOfSteps = MIN_EPISODE_STEPS;
		}
		logger.info("Initial number of errors in model: " + errorsToFix.size() + "\nMaximum number of steps per episode: "
				+ numberOfSteps + "\nErrors to fix: " + errorsToFix.toString());
		logger.info("Initializing Q-table for the errors.");
		Set<Integer> unsupportedErrors = modelProcessor.initializeQTableForErrorsInModel(model);
		for (Integer errorCode : unsupportedErrors) {
			logger.warning(
					"Encountered error that could not be resolved. Adding to unsupported errors.\nCode: " + errorCode);
			model.getModelType().addUnsupportedErrorCode(errorCode);
		}

		logger.info("Number of episodes: " + numberOfEpisodes);
		while (episode < numberOfEpisodes) {
			File episodeModelFile = createDuplicateFile(duplicateFile,
					originalModel.getParent() + "parmorel_temp_solution_" + episode + "_" + originalModel.getName());

			Model episodeModel = getModel(episodeModelFile);
			Solution sequence = handleEpisode(episodeModel, episode);

			if (sequence != null) {
				episodeModel.save();
				episodeModelFile.deleteOnExit();
				sequence.setModel(episodeModelFile);
			} else {
				episodeModelFile.delete();
			}

			// RESET initial model and extract actions + errors
			errorsToFix.clear();
			errorsToFix.addAll(originalErrors);
			episode++;
		}
		rewardCalculator.rewardPostRepair(possibleSolutions);
		Solution bestSequence = findSolutionWithHighestWeight(possibleSolutions);
		duplicateFile.delete();

		long endTime = System.currentTimeMillis();
		long executionTime = (endTime - startTime);
		logger.info("Time repairing model: " + executionTime + " ms");
		logger.info("\n-----------------ALL SEQUENCES FOUND-------------------" + "\nSIZE: " + possibleSolutions.size()
				+ "\nDISCARDED SEQUENCES: " + discardedSequences
				+ "\n--------::::B E S T   S E Q U E N C E   I S::::---------\n" + bestSequence + " with "
				+ bestSequence.getSequence().size() + " actions.");
//		removeSolutionsWithSameResult(solvingMap);

		if (bestSequence.getSequence().size() != 0) {
			bestSequence.reward(false);
		}
		saveKnowledge();
		return bestSequence;
	}

	/**
	 * Logs all encountered unsupported errors with a warning and removes them from the errorsToFix.
	 * @param model 
	 */
	private void handleUnsupportedErrors(Model model) {
		List<Error> unsupported = new ArrayList<>();
		for (Error e : errorsToFix) {
			if (model.getModelType().doesNotSupportError(e.getCode())) {
				logger.warning(
						"The error code " + e.getCode() + " for the error " + e.getMessage() + " is not supported.");
				unsupported.add(e);
			}
		}
		errorsToFix.removeAll(unsupported);
	}

	/**
	 * Copies the file and get the model from the new file.
	 * 
	 * @return the model
	 */
	protected abstract Model initializeModelFromFile();

	/**
	 * Gets the model from the file
	 * 
	 * @param model file
	 * @return the model
	 */
	protected abstract Model getModel(File model);

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
		} catch (FileAlreadyExistsException e) {
			if (destinationFile.toPath().toString().contains("temp")) {
				destinationFile.delete();
				try {
					Files.copy(fileToCopy.toPath(), destinationFile.toPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
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
	protected File createDuplicateFile() {
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
	 * @param episodeModel
	 * @param episode
	 */
	private Solution handleEpisode(Model episodeModel, int episode) {
		Solution solution = initializeSolution();
		boolean errorOcurred = false;
		int totalReward = 0;
		int step = 0;

		while (step < numberOfSteps) {
			while (!errorsToFix.isEmpty() && episodeModel.getModelType().doesNotSupportError(errorsToFix.get(0).getCode())) {
				errorsToFix.remove(0);
			}
			if (!errorsToFix.isEmpty()) {
				Error currentErrorToFix = errorsToFix.get(0);
				try {
					logger.info("EPISODE " + episode + ", STEP " + step + ", Fixing error "
							+ currentErrorToFix.getCode() + ": " + currentErrorToFix.getMessage());
					totalReward += handleStep(episodeModel, solution, episode, currentErrorToFix);
				} catch (UnsupportedErrorException e) {
					logger.warning("Encountered error that could not be resolved. Adding to unsupported errors.\nCode: "
							+ currentErrorToFix.getCode() + "\nMessage: " + currentErrorToFix.getMessage());
					episodeModel.getModelType().addUnsupportedErrorCode(e.getErrorCode());
					errorsToFix.remove(0);
				}
				step++;
			} else {
				break;
			}
		}

		int val;
		if (solution.getSequence().size() > 7) {
			val = checkForLoopsIn(solution.getSequence());
			if (val > 1) {
				totalReward -= val * 1000;
			}
		}

		solution.setWeight(totalReward);
		solution.setOriginal(originalModel);
		solution.setRewardCalculator(rewardCalculator);

		if (!errorOcurred && uniqueSequence(solution) && !solution.getSequence().isEmpty()) {
			possibleSolutions.add(solution);
		} else {
			discardedSequences++;
			solution = null;
		}

		logger.info("EPISODE " + episode + " TOTAL REWARD " + totalReward + "\n\n\n");
		return solution;
	}

	/**
	 * Initializes a new solution-object of the correct type.
	 * 
	 * @return a new initializes solution
	 */
	protected abstract Solution initializeSolution();

	/**
	 * Handles a single step
	 * 
	 * @param episodeModel
	 * @param sequence
	 * @param episode
	 * @param currentErrorToFix
	 * @return the reward from the step
	 * @throws UnsupportedErrorException if the error code is not in the Q-table,
	 *                                   and cannot be added
	 */
	private int handleStep(Model episodeModel, Solution sequence, int episode, Error currentErrorToFix)
			throws UnsupportedErrorException {
		if (!qTable.containsErrorCode(currentErrorToFix.getCode())) {
			logger.info("Error " + currentErrorToFix.getCode() + ", " + currentErrorToFix.getMessage()
					+ ", does not exist in Q-table. Attempting to solve...");
			errorsToFix = errorExtractor.extractErrorsFrom(episodeModel.getRepresentationCopy(), false);
			actionExtractor.extractActionsFor(errorsToFix);
			modelProcessor.initializeQTableForErrorsInModel(episodeModel);
			if (!qTable.containsErrorCode(currentErrorToFix.getCode())) {
				logger.info("Action for error code not found.");
				throw new UnsupportedErrorException("No action found for error code " + currentErrorToFix.getCode(),
						currentErrorToFix.getCode());
			} else {
				logger.info("Action for error code found and added to Q-table.");
			}
		}

		rewardCalculator.initializePreferencesBeforeChoosingAction(episodeModel);
		Action action = chooseAction(currentErrorToFix);
		logger.info("Chose action " + action.getMessage() + " in context " + action.getHierarchy() + " with weight "
				+ action.getWeight());
//		int sizeBefore = errorsToFix.size();
		double alpha = ALPHAS[episode];

		errorsToFix.clear();
		errorsToFix = modelProcessor.tryApplyAction(currentErrorToFix, action, episodeModel);
		reward = rewardCalculator.calculateRewardFor(episodeModel, currentErrorToFix, action);
		// Insert stuff into sequence
		sequence.setId(episode);
		List<AppliedAction> appliedActions = sequence.getSequence();
		appliedActions.add(new AppliedAction(currentErrorToFix, action));

		sequence.setSequence(appliedActions);

		int code = action.getHierarchy();

//		reward = rewardCalculator.rewardPostApplyingAction(reward, sizeBefore, errorsToFix.size(), currentErrorToFix,
//				code, action);

		if (!errorsToFix.isEmpty()) {
			Error nextErrorToFix = errorsToFix.get(0);
			logger.info("Next error code: " + nextErrorToFix.getCode());
			if (!qTable.containsErrorCode(nextErrorToFix.getCode())) {
				logger.info("Error " + nextErrorToFix.getCode() + ", " + nextErrorToFix.getMessage()
						+ ", does not exist in Q-table. Attempting to solve...");
				errorsToFix = errorExtractor.extractErrorsFrom(episodeModel.getRepresentation(), false);
				actionExtractor.extractActionsFor(errorsToFix);
				modelProcessor.initializeQTableForErrorsInModel(episodeModel);
				if (!qTable.containsErrorCode(nextErrorToFix.getCode())) {
					logger.info("Action for error code not found.");
				} else {
					logger.info("Action for error code found and added to Q-table.");
				}
			}

//			reward = rewardCalculator.updateIfNewErrorIsIntroduced(reward, initialErrorCodes, nextErrorToFix);

			nextErrorToFix = errorsToFix.get(0);
			Action a;
			try {
				a = knowledge.getOptimalActionForErrorCode(nextErrorToFix.getCode());
				int code2 = a.getHierarchy();
				double value = qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode())
						+ alpha * (reward + GAMMA * qTable.getWeight(nextErrorToFix.getCode(), code2, a.getCode())
								- qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode()));

				logger.info("Calculating new Q-value:\nOld Q-value: "
						+ qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode()) + "\nAlpha: " + alpha
						+ "\n" + "Gamma: " + GAMMA + "\nReward: " + reward + "\nNext optimal action Q-value: "
						+ qTable.getWeight(nextErrorToFix.getCode(), code2, a.getCode()) + "\n"
						+ qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode()) + " + " + alpha + " * ("
						+ reward + " + " + GAMMA + " * "
						+ qTable.getWeight(nextErrorToFix.getCode(), code2, a.getCode()) + " - "
						+ qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode()) + ") = " + value);
				qTable.setWeight(currentErrorToFix.getCode(), code, action.getCode(), value);
				logger.info(
						"Updated Q-table for error " + currentErrorToFix.getCode() + ", context " + code + ", action "
								+ action.getCode() + " " + action.getMessage() + " to new weight " + value + "\n\n");
			} catch (UnsupportedErrorException e) {
				// next error is not in the Q-table
			}

			currentErrorToFix = nextErrorToFix;
		} // it has reached the end

		else {
			double value = qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode())
					+ alpha * (reward - qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode()));

			logger.info("Calculating new Q-value:\nOld Q-value: "
					+ qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode()) + "\nAlpha: " + alpha + "\n"
					+ "\nReward: " + reward + "\n"
					+ qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode()) + " + " + alpha + " * ("
					+ reward + " - " + qTable.getWeight(currentErrorToFix.getCode(), code, action.getCode()) + ") = "
					+ value);
			qTable.setWeight(currentErrorToFix.getCode(), code, action.getCode(), value);
			logger.info("Updated Q-table for error " + currentErrorToFix.getCode() + ", context " + code + ", action "
					+ +action.getCode() + " " + action.getMessage() + " to new weight " + value);
		}

		return reward;
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

	/**
	 * Finds the solution with the highest weight from the list passed as parameter.
	 * If the list is empty, an empty solution is returned.
	 * 
	 * @param solutions
	 * @return the solution with the highest weight, or an empty solution if the
	 *         list is empty.
	 */
	private Solution findSolutionWithHighestWeight(List<Solution> solutions) {
		Solution highWeightSolution = initializeSolution();
		if (!solutions.isEmpty()) {
			highWeightSolution = solutions.get(0);

			for (int i = 1; i < solutions.size(); i++) {
				if (highWeightSolution.compareTo(solutions.get(i)) < 0) {
					highWeightSolution = solutions.get(i);
				}
			}
		}

		return highWeightSolution;
	}

	@Override
	public List<Solution> getPossibleSolutions() {
		List<Solution> solutions = new ArrayList<>(possibleSolutions);
		solutions.sort(Collections.reverseOrder());
		return solutions;
	}
}