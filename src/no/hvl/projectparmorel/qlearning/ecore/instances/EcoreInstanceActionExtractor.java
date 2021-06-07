package no.hvl.projectparmorel.qlearning.ecore.instances;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.ecore.EcoreActionExtractor;
import no.hvl.projectparmorel.qlearning.knowledge.QTable;

import java.util.ArrayList;
import java.util.List;

public class EcoreInstanceActionExtractor extends EcoreActionExtractor {

	public EcoreInstanceActionExtractor() {
		super();
	}
	
	@Override
	public List<Action> extractActionsNotInQTableFor(QTable qTable, List<Error> errors) {
		List<Action> actions = new ArrayList<>();
		List<Error> copyOfErrors = new ArrayList<>(errors);
		int j = 0;
		for (Error error : errors) {
			if (error.getCode() == 0) {
				// Is OCL constraint violation, so analyse error and produce action.
				copyOfErrors.remove(j--);
			}
			j++;
		}

		if (!copyOfErrors.isEmpty()) { // Process all non-OCL constraint violations, if any.
			List<Action> remainingActions = super.extractActionsNotInQTableFor(qTable, copyOfErrors);
			actions.addAll(remainingActions);
		}

		return actions;
	}

}
