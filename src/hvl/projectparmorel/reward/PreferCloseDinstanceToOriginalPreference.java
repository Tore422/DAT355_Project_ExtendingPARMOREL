package hvl.projectparmorel.reward;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.modelrepair.Solution;

public class PreferCloseDinstanceToOriginalPreference extends Preference implements SolutionPreference {

	PreferCloseDinstanceToOriginalPreference() {
		super(-1, PreferenceOption.PREFER_CLOSE_DISTANCE_TO_ORIGINAL);
	}

	@Override
	int rewardActionForError(Model model, Error error, Action action) {
		return weight;
	}

	@Override
	public int rewarcalculateRewardFor(Solution solution, Model model, QTable qTable) {
		double distance = solution.calculateDistanceFromOriginal();
		if(distance >= 0) {
			double reward = 100 - distance;
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
