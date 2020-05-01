package hvl.projectparmorel.reward;

import java.util.List;
import java.util.logging.Logger;

import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.qlearning.Action;
import hvl.projectparmorel.qlearning.AppliedAction;
import hvl.projectparmorel.qlearning.Error;
import hvl.projectparmorel.qlearning.Model;
import hvl.projectparmorel.qlearning.QModelFixer;
import hvl.projectparmorel.qlearning.QSolution;

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
	public void rewardPostRepair(List<QSolution> possibleSolutions, QTable qTable) {
		QSolution optimalSequence = null;
		int smallestSequenceSize = 9999;

		for (QSolution sequence : possibleSolutions) {
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
	private void rewardSolution(QSolution solution, QTable qTable) {
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
