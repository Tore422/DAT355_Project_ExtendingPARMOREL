package no.hvl.projectparmorel.qlearning.ecore;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.ActionExtractor;
import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.SerializableMethod;
import no.hvl.projectparmorel.qlearning.knowledge.QTable;

public class EcoreActionExtractor implements ActionExtractor {
	
	public EcoreActionExtractor() {
	}
	
	@Override
	public List<Action> extractActionsNotInQTableFor(QTable qTable, List<Error> errors) {
		Map<Integer, Action> actionsFound = new HashMap<Integer, Action>();

		for (Error error : errors) {
			if (!qTable.containsErrorCode(error.getCode())) {
				List<?> contexts = error.getContexts();
				actionsFound = addMethodsFromContextList(actionsFound, contexts);
			}
		}
		return new ArrayList<>(actionsFound.values());
	}

	/**
	 * Copies the actions given as input, adds the methods provided by the contexts
	 * if they can help solve the problem, and returns the combined result.
	 * 
	 * @param actions
	 * @param contexts
	 * @return the actions found in the context that can help solve a problem
	 */
	private Map<Integer, Action> addMethodsFromContextList(Map<Integer, Action> actions, List<?> contexts) {
		Map<Integer, Action> actionsFound = new HashMap<>(actions);
		for (int i = 0; i < contexts.size() - 1; i++) {
			if (contexts.get(i) != null) {
				Class<? extends Object> context = contexts.get(i).getClass();
				addMethodsFromContext(actionsFound, context, i);
			}
		}
		return actionsFound;
	}

	/**
	 * Copies the actions given as input, adds the methods provided by the context
	 * if they can help solve the problem, and returns the combined result.
	 * 
	 * @param actions
	 * @param context
	 * @param hierarchy
	 * @return a map containing the actions from the specified context if they
	 *         result in an altered model.
	 */
	private void addMethodsFromContext(Map<Integer, Action> actions, Class<? extends Object> context, int hierarchy) {
		if (context != EPackageImpl.class) { // if not package
			Method[] methods = context.getMethods();
			for (Method method : methods) {
				if (methodCanPerformChange(method) && !actions.containsKey(method.hashCode())) {
					Action action = new EcoreAction(method.hashCode(), method.getName(), new SerializableMethod(method),
							hierarchy + 1);
					actions.put(method.hashCode(), action);
				}
			}

			if (!actions.containsKey(99999)) {
				Action a = new EcoreAction(99999, "delete", null, hierarchy + 1);
				actions.put(99999, a);
			}
		}
	}

	/**
	 * Checks that the method actually can perform a change. Get-methods and so on
	 * will return false.
	 * 
	 * @param method
	 * @return true if the method can alter the model, false otherwise
	 */
	private boolean methodCanPerformChange(Method method) {
		return !method.getName().startsWith("is") && !method.getName().startsWith("get")
				&& !method.getName().startsWith("to") && !method.getName().startsWith("e")
				&& !method.getName().contains("Get") && !method.getName().contains("Is")
				&& !method.getName().contentEquals("eDynamicIsSet") && !method.getName().contentEquals("dynamicGet")
				&& !method.getName().contentEquals("hashCode") && !method.getName().contentEquals("eVirtualIsSet")
				&& !method.getName().contentEquals("dynamicUnset") && !method.getName().contentEquals("wait")
				&& !method.getName().contentEquals("eDynamicUnset") && !method.getName().contentEquals("notify")
				&& !method.getName().contentEquals("notifyAll") && !method.getName().contentEquals("eVirtualGet")
				&& !method.getName().contentEquals("eVirtualUnset") && !method.getName().contentEquals("eDynamicGet")
				&& !method.getName().contentEquals("dynamicSet");
	}
}
