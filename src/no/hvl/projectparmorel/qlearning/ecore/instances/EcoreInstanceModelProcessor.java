package no.hvl.projectparmorel.qlearning.ecore.instances;

import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.*;
import no.hvl.projectparmorel.qlearning.ecore.*;
import no.hvl.projectparmorel.qlearning.knowledge.Knowledge;
import no.hvl.projectparmorel.qlearning.knowledge.QTable;
import org.eclipse.emf.common.notify.impl.NotificationChainImpl;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class EcoreInstanceModelProcessor extends EcoreModelProcessor {

	public EcoreInstanceModelProcessor(Knowledge knowledge) {
		super(knowledge);
		errorExtractor = new EcoreInstanceErrorExtractor();
	}

	@Override
	public Set<Integer> initializeQTableForErrorsInModel(Model model) {
		if (model instanceof EcoreInstanceModel) {
			return initializeQTableForErrorsInModel((EcoreInstanceModel) model);
		} else {
			throw new IllegalArgumentException(
					"The method must be called with a model of type hvl.projectparmorel.ecore.EcoreInstanceModel");
		}
	}

	/**
	 * Goes through all the errors in the model, and if the error is not in the
	 * q-table is is added along with matching actions. Alternatively the error code
	 * is added to a set of unsupported errors.
	 * 
	 * @param instanceModel
	 * @param destinationURI
	 * @return a set of unsupported error codes that was not added to the Q-table
	 */
	private Set<Integer> initializeQTableForErrorsInModel(EcoreInstanceModel instanceModel) {
		errors = errorExtractor.extractErrorsFrom(instanceModel.getRepresentationCopy(), false);

		ActionExtractor actionExtractor = new EcoreActionExtractor();
		List<Action> possibleActions = actionExtractor.extractActionsNotInQTableFor(knowledge.getQTable(), errors);

		Set<Integer> unsupportedErrors = new HashSet<>();

		for (Error error : errors) {
			if(ModelType.ECORE_INSTANCE.doesNotSupportError(error.getCode())) {
				unsupportedErrors.add(error.getCode());
			} else if (!knowledge.getQTable().containsErrorCode(error.getCode())
					&& !unsupportedErrors.contains(error.getCode())) {
				boolean actionForErrorFound = false;
				for (int i = 0; i < error.getContexts().size(); i++) {
					if (error.getContexts().get(i) != null) {
						for (Action action : possibleActions) {
							if (isInvokable(error, error.getContexts().get(i).getClass(), action)) {
								Resource modelCopy = (Resource) instanceModel.getRepresentationCopy();
								List<Error> newErrors = tryApplyAction(error, action, modelCopy, i);
								if (newErrors != null && !errorStillExists(newErrors, error)) {
									actionForErrorFound = true;
									Action newAction = new EcoreAction(action.getId(), action.getName(),
											action.getMethod(), i);
									initializeQTableForAction(error, newAction);
								}
							}
						}
					}
				}
				if (!actionForErrorFound) {
					unsupportedErrors.add(error.getCode());
				}
			}
		}
		return unsupportedErrors;
	}

	/**
	 * Checks that the action is invokable for the given class and error
	 * 
	 * @param error
	 * @param class1
	 * @param action
	 * @return true if the action is invokable, false otherwise
	 */
	private boolean isInvokable(Error error, Class<? extends Object> class1, Action action) {
		Method[] methods = class1.getMethods();
		if (action.isDelete()) {
			return true;
		} else {
			if (action.handlesMissingArgumentForGenericType(error) && class1 != EClassImpl.class) {
				return true;
			} else {
				for (Method method : methods) {
					if (action.getMethod().getMethod() != null
							&& action.getMethod().getMethod().hashCode() == method.hashCode()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public List<Error> tryApplyAction(Error error, Action action, Model model) {
		if (model instanceof EcoreInstanceModel) {
			return tryApplyAction(error, action, (Resource) model.getRepresentation(), action.getContextId());
		}
		throw new IllegalArgumentException("The model needs to be of type org.eclipse.emf.ecore.resource.Resource");
	}

	/**
	 * Extracts package content from the model, and matches the location where the
	 * error resides to the correct type and tries to apply the action to this error
	 * location.
	 * 
	 * @param error
	 * @param action
	 * @param model
	 * @param hierarchy
	 * @return a list of new errors if the action was successfully applied, null
	 *         otherwise
	 */
	private List<Error> tryApplyAction(Error error, Action action, Resource model, int hierarchy) {
		// Casting instance model element throws ClassCastException.
		// Need to find a way to represent instance model elements, to apply actions to fix errors.
		EPackage ePackage = (EPackage) model.getContents().get(error.getPackageIndex()); 
		EObject object = (EObject) error.getContexts().get(hierarchy);

		if (object != null) {
			boolean success = false;
			for (int i = 0; i < ePackage.getEClassifiers().size() && !success; i++) {
				success = identifyObjectTypeAndApplyAction(error, action, object, ePackage.getEClassifiers().get(i));
			}
			for(int i = 0; i < ePackage.getESubpackages().size() && !success; i++) {
				EPackage epa = ePackage.getESubpackages().get(i);
				for (int j = 0; j < epa.getEClassifiers().size() && !success; j++) {
					success = identifyObjectTypeAndApplyAction(error, action, object, epa.getEClassifiers().get(j));
				}
			}
			List<Error> newErrors = errorExtractor.extractErrorsFrom(model, false);
			return newErrors;
		}
		return null;
	}

	/**
	 * Checks if the error still exists, meaning we did not accomplish anything.
	 * 
	 * @param newErrors
	 * @param error
	 * @return true if the error still exists
	 */
	private boolean errorStillExists(List<Error> newErrors, Error error) {
		int errorCode = error.getCode();
		int numberOfErrorCodesOriginally = countNumberOfMatchingErrorCodes(errors, errorCode);
		int numberOfErrorCodesNow = countNumberOfMatchingErrorCodes(newErrors, errorCode);
		return numberOfErrorCodesNow >= numberOfErrorCodesOriginally;
	}

	private int countNumberOfMatchingErrorCodes(List<Error> errorsToCount, int errorCode) {
		int numberOfMatchingErrorCodes = 0;
		for (Error error : errorsToCount) {
			if (error.getCode() == errorCode) {
				numberOfMatchingErrorCodes++;
			}
		}
		return numberOfMatchingErrorCodes;
	}






	/**
	 * Initializes the QTable for the specified action, setting an initial weight.
	 * 
	 * @param error
	 * @param action
	 */
	private void initializeQTableForAction(Error error, Action action) {
		QTable actionDirectory = knowledge.getQTable();

		int contextId = action.getContextId();

		if (!actionDirectory.containsActionForErrorAndContext(error.getCode(), contextId, action.getId())) {
			action.setWeight(0);
			actionDirectory.setAction(error.getCode(), contextId, action);
		}
	}





	private static final Random RANDOM = new Random();

	/**
	 * Simulates type preferences
	 * 
	 * @param list
	 * @return a list of default values for arguments
	 */
	private Object[] getDefaultValues(List<String> list) {
		List<Object> values = new ArrayList<>();
		for (String element : list) {
			if (element.contentEquals("int")) {
				values.add(1);
			}
			if (element.contentEquals("boolean")) {
				values.add(false);
			}
			if (element.contentEquals("booleanTRUE")) {
				values.add(true);
			}
			if (element.contains("String")) {
				values.add("placeholder" + RANDOM.nextInt((999999 - 1) + 1) + 1);
			}
			if (element.contentEquals("org.eclipse.emf.ecore.EClassifier")) {
				values.add(EcorePackage.Literals.ESTRING);
			}
			if (element.contentEquals("org.eclipse.emf.ecore.EClassifierCLASS")) {
				values.add(EcorePackage.Literals.ECLASS);
			}
			if (element.contentEquals("org.eclipse.emf.common.notify.NotificationChain")) {
				values.add(new NotificationChainImpl());
			}
			if (element.contains("TypeParameter")) {
				values.add(EcoreFactory.eINSTANCE.createETypeParameter());
			}
			if (element.contains("Reference")) {
				values.add(EcorePackage.Literals.EREFERENCE__EREFERENCE_TYPE);
			}
			if (element.contains("Literal")) {
				values.add(null);
			}
		}
		Object[] val = new Object[values.size()];
		val = values.toArray(val);

		return val;
	}
}
