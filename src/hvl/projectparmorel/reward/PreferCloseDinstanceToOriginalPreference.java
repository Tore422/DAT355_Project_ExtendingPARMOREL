package hvl.projectparmorel.reward;

import java.util.logging.Logger;

import org.junit.platform.commons.util.ExceptionUtils;

import hvl.projectparmorel.exceptions.DistanceUnavailableException;
import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.modelrepair.QModelFixer;
import hvl.projectparmorel.modelrepair.Solution;

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
	public int rewarcalculateRewardFor(Solution solution, Model model, QTable qTable) {
		long startTime = System.currentTimeMillis();
		try {
			double distance = solution.calculateDistanceFromOriginal();
			long measureTime = System.currentTimeMillis() - startTime;
			log.info("Time to measure distance: " + measureTime + " ms");
			int reward = (int)(200 - distance);
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
