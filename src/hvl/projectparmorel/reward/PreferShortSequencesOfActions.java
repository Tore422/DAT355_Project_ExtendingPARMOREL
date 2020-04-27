package hvl.projectparmorel.reward;

import java.util.List;
import java.util.logging.Logger;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.modelrepair.QModelFixer;
import hvl.projectparmorel.modelrepair.Solution;

class PreferShortSequencesOfActions extends Preference implements PostRepairPreference {

	private Logger logger;

	PreferShortSequencesOfActions(int weight) {
		super(weight, PreferenceOption.SHORT_SEQUENCES_OF_ACTIONS);
		logger = Logger.getLogger(QModelFixer.LOGGER_NAME);
	}

	/**
	 * Rewards the best shortest sequence in the specified list of sequences
	 */
	@Override
	public void rewardPostRepair(List<Solution> possibleSolutions, QTable qTable) {
		Solution optimalSequence = null;
		int smallestSequenceSize = 9999;

		for (Solution sequence : possibleSolutions) {
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
			optimalSequence.setWeight(optimalSequence.getWeight() + weight);
			rewardSolution(optimalSequence, qTable);
			logger.info("Rewarded solution " + optimalSequence.getId() + " with a weight of " + weight
					+ " because of preferences to reward shorter sequences.");
		}
	}

	/**
	 * Rewards the specified sequence.
	 * 
	 * @param solution
	 * @param knowledge
	 */
	private void rewardSolution(Solution solution, QTable qTable) {
		for (AppliedAction appliedAction : solution.getSequence()) {
			int contextId = appliedAction.getAction().getContextId();
			int errorCode = appliedAction.getError().getCode();
			int actionId = appliedAction.getAction().getId();
			double oldWeight = qTable.getWeight(errorCode, contextId, actionId);

			qTable.setWeight(errorCode, contextId, actionId, oldWeight + 300);
			if (qTable.getTagDictionaryForAction(errorCode, contextId, actionId).contains(preferenceOption)) {
				int oldTagValue = qTable.getTagDictionaryForAction(errorCode, contextId, actionId)
						.getWeightFor(preferenceOption);
				qTable.setTagValueInTagDictionary(errorCode, contextId, actionId, preferenceOption.id, oldTagValue + 500);
			} else {
				qTable.setTagValueInTagDictionary(errorCode, contextId, actionId, preferenceOption.id, 500);
			}
			qTable.updateReward(appliedAction, contextId);
		}
	}

	@Override
	int rewardActionForError(Model model, Error error, Action action) {
		return 0;
	}

}
