package no.hvl.projectparmorel.qlearning.reward;

import java.util.logging.Logger;

import org.junit.platform.commons.util.ExceptionUtils;

import no.hvl.projectparmorel.exceptions.DistanceUnavailableException;
import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.AppliedAction;
import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.Model;
import no.hvl.projectparmorel.qlearning.QModelFixer;
import no.hvl.projectparmorel.qlearning.QSolution;
import no.hvl.projectparmorel.qlearning.knowledge.QTable;

public class PreferCloseDinstanceToOriginalPreference extends Preference implements SolutionPreference {

	private Logger log;

	PreferCloseDinstanceToOriginalPreference() {
		super(-1, PreferenceOption.PREFER_CLOSE_DISTANCE_TO_ORIGINAL);
		log = Logger.getLogger(QModelFixer.LOGGER_NAME);
	}

	@Override
	int rewardActionForError(Model model, Error error, Action action) {
		return weight;
	}

	@Override
	public int rewardcalculateRewardFor(QSolution solution, Model model, QTable qTable) {
		long startTime = System.currentTimeMillis();
		try {
			double distance = solution.calculateDistanceFromOriginal();
			long measureTime = System.currentTimeMillis() - startTime;
			log.info("Time to measure distance: " + measureTime + " ms");
			int reward = (int)(distance);
			for (AppliedAction appliedAction : solution.getSequence()) {
				QModelFixer.updateQTable(qTable, appliedAction.getError().getCode(), appliedAction.getAction().getContextId(), appliedAction.getAction().getId(), reward);
			}
			return (int) (reward * solution.getSequence().size());
		} catch (DistanceUnavailableException e) {
			log.warning("Could not calculate the distance between the models because of a "
					+ e.getCause().getClass().getName() + "\nStack trace:\n"
					+ ExceptionUtils.readStackTrace(e.getCause()));
		}
		return 0;
	}

}
