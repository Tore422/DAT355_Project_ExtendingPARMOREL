package no.hvl.projectparmorel.qlearning;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import no.hvl.projectparmorel.ModelFixer;
import no.hvl.projectparmorel.Solution;
import no.hvl.projectparmorel.exceptions.NoErrorsInModelException;
import no.hvl.projectparmorel.exceptions.UnsupportedErrorException;
import no.hvl.projectparmorel.qlearning.knowledge.Knowledge;
import no.hvl.projectparmorel.qlearning.knowledge.QTable;
import no.hvl.projectparmorel.qlearning.reward.PreferenceOption;
import no.hvl.projectparmorel.qlearning.reward.RewardCalculator;

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
	
	/**
	 * The learning rate of the algorithm
	 */
	protected static final double ALPHA = 1.0;
	/**
	 * Eagerness - 0 looks in the near future, 1 looks in the distant future
	 */
	protected final static double GAMMA = 1.0;
	
	protected ActionExtractor actionExtractor;
	protected ErrorExtractor errorExtractor;
	protected ModelProcessor modelProcessor;
	protected RewardCalculator rewardCalculator;
	protected Knowledge knowledge;
	
	private final int MIN_EPISODE_STEPS = 12;
	private QTable qTable;
	private static Logger LOGGER = Logger.getLogger(LOGGER_NAME);
	private double randomFactor = 0.25;
	private int numberOfEpisodes = 25;
	private List<Error> errorsToFix;
	private int discardedSequences;
	private int reward = 0;
	private int numberOfSteps;
	protected File originalModel;
	private List<Error> originalErrors;
	private List<Integer> initialErrorCodes;
	private List<QSolution> possibleSolutions;

	public QModelFixer() {
		errorsToFix = new ArrayList<Error>();
		knowledge = new Knowledge();
		qTable = knowledge.getQTable();
		discardedSequences = 0;
		originalErrors = new ArrayList<Error>();
		initialErrorCodes = new ArrayList<Integer>();
		possibleSolutions = new ArrayList<QSolution>();
		rewardCalculator = new RewardCalculator(knowledge, new ArrayList<>());
		numberOfSteps = MIN_EPISODE_STEPS;
		loadKnowledge();
		actionExtractor = initializeActionExtractor();
		errorExtractor = initializeErrorExtractor();
		modelProcessor = initializeModelProcessor();
	}

	/**
	 * Gets an {@link ActionExtractor} that allows the algorithm to extract actions
	 * that can be used on the model.
	 * 
	 * @return a meta model specific ActionExtractor that can extract actions that
	 *         can be applied to the model
	 */
	protected abstract ActionExtractor initializeActionExtractor();

	/**
	 * Gets an {@link ErrorExtractor} that allows the algorithm to extract errors
	 * from the model.
	 * 
	 * @return a meta model specific ErrorExtractor that can get errors from the
	 *         model.
	 */
	protected abstract ErrorExtractor initializeErrorExtractor();

	/**
	 * Gets a {@link ModelProcessor} that can be used to process and apply actions
	 * to the model.
	 * 
	 * @return a meta model specific ModelProcessor.
	 */
	protected abstract ModelProcessor initializeModelProcessor();

	public QModelFixer(List<PreferenceOption> preferences) {
		this();
		rewardCalculator = new RewardCalculator(knowledge, preferences);
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
			LOGGER.info("Choosing random action.");
			return knowledge.getQTable().getRandomActionForError(error.getCode());
		} else {
			LOGGER.info("Choosing optimal action");
			return knowledge.getOptimalActionForErrorCode(error.getCode());
		}
	}

	@Override
	public Solution fixModel(File modelFile) throws NoErrorsInModelException {
		long startTime = System.currentTimeMillis();
		LOGGER.info("Repairing " + modelFile.getName());
		originalModel = modelFile;
		Model model = initializeModelFromFile();
		rewardCalculator.initializePreferencesFor(model);

		File duplicateFile = createDuplicateFile();

		LOGGER.info("Running with preferences " + rewardCalculator.getPreferences().toString());

		discardedSequences = 0;
		int episode = 0;

		errorsToFix = errorExtractor.extractErrorsFrom(model.getRepresentation(), true);
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
		LOGGER.info(
				"Initial number of errors in model: " + errorsToFix.size() + "\nMaximum number of steps per episode: "
						+ numberOfSteps + "\nErrors to fix: " + errorsToFix.toString());
		LOGGER.info("Initializing Q-table for the errors.");
		Set<Integer> unsupportedErrors = modelProcessor.initializeQTableForErrorsInModel(model);
		for (Integer errorCode : unsupportedErrors) {
			LOGGER.warning(
					"Encountered error that could not be resolved. Adding to unsupported errors.\nCode: " + errorCode);
			model.getModelType().addUnsupportedErrorCode(errorCode);
		}

		LOGGER.info("Number of episodes: " + numberOfEpisodes);
		while (episode < numberOfEpisodes) {
			File episodeModelFile = createDuplicateFile(duplicateFile,
					originalModel.getParent() + "parmorel_temp_solution_" + episode + "_" + originalModel.getName());

			Model episodeModel = getModel(episodeModelFile);
			QSolution solution = handleEpisode(episodeModel, episode);
			solution.setModel(episodeModelFile);
			episodeModel.save();

			double totalReward = solution.getWeight();
			totalReward += rewardCalculator.calculateRewardFor(episodeModel, solution);
			solution.setWeight(totalReward);
			solution.setRewardCalculator(rewardCalculator);

			if (isUnique(solution) && !solution.getSequence().isEmpty()) {
				possibleSolutions.add(solution);
				episodeModelFile.deleteOnExit();
				LOGGER.info("Solution added to possible solitons: " + solution.getSequence().toString());
			} else {
				LOGGER.info("Solution discarded.");
				discardedSequences++;
				solution = null;
				episodeModelFile.delete();
			}

			LOGGER.info("EPISODE " + episode + " TOTAL REWARD " + totalReward + "\n\n\n");

			// RESET initial model and extract actions + errors
			errorsToFix.clear();
			errorsToFix.addAll(originalErrors);
			episode++;
		}
		rewardCalculator.rewardPostRepair(possibleSolutions);
		QSolution bestSequence = findSolutionWithHighestWeight(possibleSolutions);
		duplicateFile.delete();

		long endTime = System.currentTimeMillis();
		long executionTime = (endTime - startTime);
		LOGGER.info("Time repairing model: " + executionTime + " ms");
		LOGGER.info("\n-----------------ALL SEQUENCES FOUND-------------------" + "\nSIZE: " + possibleSolutions.size()
				+ "\nDISCARDED SEQUENCES: " + discardedSequences
				+ "\n--------::::B E S T   S E Q U E N C E   I S::::---------\n" + bestSequence + " with "
				+ bestSequence.getSequence().size() + " actions.");

		saveKnowledge();
		return bestSequence;
	}

	/**
	 * Logs all encountered unsupported errors with a warning and removes them from
	 * the errorsToFix.
	 * 
	 * @param model
	 */
	private void handleUnsupportedErrors(Model model) {
		List<Error> unsupported = new ArrayList<>();
		for (Error e : errorsToFix) {
			if (model.getModelType().doesNotSupportError(e.getCode())) {
//				logger.warning(
//						"The error code " + e.getCode() + " for the error " + e.getMessage() + " is not supported.");
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
	private QSolution handleEpisode(Model episodeModel, int episode) {
		QSolution solution = initializeSolution();
		int totalReward = 0;
		int step = 0;

		while (step < numberOfSteps) {
			while (!errorsToFix.isEmpty()
					&& episodeModel.getModelType().doesNotSupportError(errorsToFix.get(0).getCode())) {
				errorsToFix.remove(0);
			}
			if (!errorsToFix.isEmpty()) {
				Error currentErrorToFix = errorsToFix.get(0);
				try {
					LOGGER.info("EPISODE " + episode + ", STEP " + step + ", Fixing error "
							+ currentErrorToFix.getCode() + ": " + currentErrorToFix.getMessage());
					totalReward += handleStep(episodeModel, solution, episode, currentErrorToFix);
				} catch (UnsupportedErrorException e) {
					LOGGER.warning("Encountered error that could not be resolved. Adding to unsupported errors.\nCode: "
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
		solution.setOriginal(originalModel);
		solution.setWeight(totalReward);
		return solution;
	}

	/**
	 * Initializes a new solution-object of the correct type.
	 * 
	 * @return a new initializes solution
	 */
	protected abstract QSolution initializeSolution();

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
	private int handleStep(Model episodeModel, QSolution sequence, int episode, Error currentErrorToFix)
			throws UnsupportedErrorException {
		if (!qTable.containsErrorCode(currentErrorToFix.getCode())) {
			LOGGER.info("Error " + currentErrorToFix.getCode() + ", " + currentErrorToFix.getMessage()
					+ ", does not exist in Q-table. Attempting to solve...");
			errorsToFix = errorExtractor.extractErrorsFrom(episodeModel.getRepresentation(), false);
			actionExtractor.extractActionsNotInQTableFor(knowledge.getQTable(), errorsToFix);
			modelProcessor.initializeQTableForErrorsInModel(episodeModel);
			if (!qTable.containsErrorCode(currentErrorToFix.getCode())) {
				LOGGER.info("Action for error code not found.");
				throw new UnsupportedErrorException("No action found for error code " + currentErrorToFix.getCode(),
						currentErrorToFix.getCode());
			} else {
				LOGGER.info("Action for error code found and added to Q-table.");
			}
		}

		rewardCalculator.initializePreferencesBeforeChoosingAction(episodeModel);
		Action action = chooseAction(currentErrorToFix);
		LOGGER.info("Chose action " + action.getName() + " in context " + action.getContextId() + " with weight "
				+ action.getWeight());

		errorsToFix.clear();
		errorsToFix = modelProcessor.tryApplyAction(currentErrorToFix, action, episodeModel);
		reward = rewardCalculator.calculateRewardFor(episodeModel, currentErrorToFix, action);

		sequence.setId(episode);
		List<AppliedAction> appliedActions = sequence.getSequence();
		appliedActions.add(new AppliedAction(currentErrorToFix, action));
		
		int context = action.getContextId();
		if (!errorsToFix.isEmpty()) {
			Error nextErrorToFix = errorsToFix.get(0);
			LOGGER.info("Next error code: " + nextErrorToFix.getCode());
			if (!qTable.containsErrorCode(nextErrorToFix.getCode())) {
				LOGGER.info("Error " + nextErrorToFix.getCode() + ", " + nextErrorToFix.getMessage()
						+ ", does not exist in Q-table. Attempting to solve...");
				errorsToFix = errorExtractor.extractErrorsFrom(episodeModel.getRepresentation(), false);
				actionExtractor.extractActionsNotInQTableFor(knowledge.getQTable(), errorsToFix);
				modelProcessor.initializeQTableForErrorsInModel(episodeModel);
				if (!qTable.containsErrorCode(nextErrorToFix.getCode())) {
					LOGGER.info("Action for error code not found.");
				} else {
					LOGGER.info("Action for error code found and added to Q-table.");
				}
			}
			
			nextErrorToFix = errorsToFix.get(0);
			Action a;
			try {
				a = knowledge.getOptimalActionForErrorCode(nextErrorToFix.getCode());
				int code2 = a.getContextId();
				double value = qTable.getWeight(currentErrorToFix.getCode(), context, action.getId())
						+ ALPHA * (reward + GAMMA * qTable.getWeight(nextErrorToFix.getCode(), code2, a.getId())
								- qTable.getWeight(currentErrorToFix.getCode(), context, action.getId()));

				LOGGER.info("Calculating new Q-value:\nOld Q-value: "
						+ qTable.getWeight(currentErrorToFix.getCode(), context, action.getId()) + "\nAlpha: " + ALPHA
						+ "\n" + "Gamma: " + GAMMA + "\nReward: " + reward + "\nNext optimal action Q-value: "
						+ qTable.getWeight(nextErrorToFix.getCode(), code2, a.getId()) + "\n"
						+ qTable.getWeight(currentErrorToFix.getCode(), context, action.getId()) + " + " + ALPHA + " * ("
						+ reward + " + " + GAMMA + " * "
						+ qTable.getWeight(nextErrorToFix.getCode(), code2, a.getId()) + " - "
						+ qTable.getWeight(currentErrorToFix.getCode(), context, action.getId()) + ") = " + value);
				qTable.setWeight(currentErrorToFix.getCode(), context, action.getId(), value);
				LOGGER.info(
						"Updated Q-table for error " + currentErrorToFix.getCode() + ", context " + context + ", action "
								+ action.getId() + " " + action.getName() + " to new weight " + value + "\n\n");
			} catch (UnsupportedErrorException e) {
				// next error is not in the Q-table
			}

			currentErrorToFix = nextErrorToFix;
		}

		else {
			updateQTable(qTable, currentErrorToFix.getCode(), context, action.getId(), reward);
		}

		return reward;
	}

	/**
	 * Updates the specified Q-Table for the given error, context and action with
	 * the new weight. The new weight will be calculated based on the old weight and
	 * on the new. This calculation is used when rewarding actions post repair.
	 * 
	 * @param qTable
	 * @param errorCode
	 * @param contextId
	 * @param actionCode
	 * @param weight
	 */
	public static void updateQTable(QTable qTable, int errorCode, int contextId, int actionCode, int weight) {
		double value = qTable.getWeight(errorCode, contextId, actionCode)
				+ ALPHA * (weight + GAMMA - qTable.getWeight(errorCode, contextId, actionCode));

		LOGGER.info("Calculating new Q-value:\nOld Q-value: " + qTable.getWeight(errorCode, contextId, actionCode)
				+ "\nAlpha: " + ALPHA + "\nGamma:" +  GAMMA + "\n" + "\nReward: " + weight + "\n"
				+ qTable.getWeight(errorCode, contextId, actionCode) + " + " + ALPHA + " * (" + weight + " + " + GAMMA + " - "
				+ qTable.getWeight(errorCode, contextId, actionCode) + ") = " + value);
		qTable.setWeight(errorCode, contextId, actionCode, value);
		LOGGER.info("Updated Q-table for error " + errorCode + ", context " + contextId + ", action " + actionCode
				+ " to new weight " + value);
	}

	/**
	 * Checks if a solution is equal to any other with respect to the other
	 * potential soluions.
	 * 
	 * @param solution
	 * @return true if the solution is unique, false otherwise
	 */
	private boolean isUnique(QSolution solution) {
		for (QSolution otherSolution : possibleSolutions) {
			if (solution.getSequence().size() == otherSolution.getSequence().size()) {
				List<AppliedAction> solutionSequence = solution.getSequence();
				List<AppliedAction> otherSequence = otherSolution.getSequence();

				boolean solutionsAreEqual = true;
				for (int i = 0; i < solution.getSequence().size() && solutionsAreEqual; i++) {
					if (!solutionSequence.get(i).getAction().equals(otherSequence.get(i).getAction())) {
						solutionsAreEqual = false;
					}
				}
				if (solutionsAreEqual) {
					LOGGER.info("Solution " + solution.getSequence().toString() + " already exists.");
					return false;
				}
			}
		}
		return true;
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
	private QSolution findSolutionWithHighestWeight(List<QSolution> solutions) {
		QSolution highWeightSolution = initializeSolution();
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