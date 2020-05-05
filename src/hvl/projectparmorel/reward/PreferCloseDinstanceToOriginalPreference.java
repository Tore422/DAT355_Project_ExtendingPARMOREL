package hvl.projectparmorel.reward;

import java.util.logging.Logger;

import org.junit.platform.commons.util.ExceptionUtils;

import hvl.projectparmorel.exceptions.DistanceUnavailableException;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.qlearning.Action;
import hvl.projectparmorel.qlearning.AppliedAction;
import hvl.projectparmorel.qlearning.Error;
import hvl.projectparmorel.qlearning.Model;
import hvl.projectparmorel.qlearning.QModelFixer;
import hvl.projectparmorel.qlearning.QSolution;

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
