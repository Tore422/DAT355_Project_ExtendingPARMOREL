package hvl.projectparmorel.reward;

import java.util.logging.Logger;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.modelrepair.QModelFixer;
import hvl.projectparmorel.modelrepair.Solution;

public class PreferMaintainability extends Preference implements SolutionPreference {

	private Logger log;
	
	PreferMaintainability() {
		super(-1, PreferenceOption.PREFER_MAINTAINABILITY);
		log = Logger.getLogger(QModelFixer.LOGGER_NAME);
	}

	@Override
	int rewardActionForError(Model model, Error error, Action action) {
		return weight;
	}

	@Override
	public int rewardcalculateRewardFor(Solution solution, Model model, QTable qTable) {
		long startTime = System.currentTimeMillis();
		double metric = solution.calculateMaintainability();
		long measureTime = System.currentTimeMillis() - startTime;
		log.info("MAINTAINABILITY: "+metric);
		log.info("Time to get metric: " + measureTime + " ms");
		if(metric > -1) {
			double reward = (100 - metric);
			for (AppliedAction appliedAction : solution.getSequence()) {
				Action action = appliedAction.getAction();
				qTable.setWeight(appliedAction.getError().getCode(), action.getHierarchy(), action.getCode(), reward);
			}
			return (int) (reward * solution.getSequence().size());
		} else {
			return 0;
		}
	}

}
