package no.hvl.projectparmorel.qlearning.reward;

import java.util.logging.Logger;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.AppliedAction;
import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.Model;
import no.hvl.projectparmorel.qlearning.QModelFixer;
import no.hvl.projectparmorel.qlearning.QSolution;
import no.hvl.projectparmorel.qlearning.knowledge.QTable;

public class PreferRelaxation extends Preference implements SolutionPreference {

	private Logger log;

	PreferRelaxation() {
		super(-1, PreferenceOption.PREFER_RELAXATION);
		log = Logger.getLogger(QModelFixer.LOGGER_NAME);
	}

	@Override
	int rewardActionForError(Model model, Error error, Action action) {
		return weight;
	}

	@Override
	public int rewardcalculateRewardFor(QSolution solution, Model model, QTable qTable) {
		long startTime = System.currentTimeMillis();
		double metric = solution.calculateRelaxation();
		long measureTime = System.currentTimeMillis() - startTime;
		log.info("Relaxation Index of the metamodel: " + metric + "\nTime to get metric: " + measureTime + " ms");
		if (metric > -1) {
			double reward = metric;
			for (AppliedAction appliedAction : solution.getSequence()) {
				Action action = appliedAction.getAction();
				qTable.setWeight(appliedAction.getError().getCode(), action.getContextId(), action.getId(), reward);
			}
			return (int) (reward * solution.getSequence().size());
		} else {
			return 0;
		}
	}

}
