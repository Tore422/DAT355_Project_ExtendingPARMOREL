package hvl.projectparmorel.ecore;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.emf.common.notify.impl.NotificationChainImpl;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.eclipse.emf.ecore.impl.EEnumLiteralImpl;
import org.eclipse.emf.ecore.impl.EGenericTypeImpl;
import org.eclipse.emf.ecore.impl.EOperationImpl;
import org.eclipse.emf.ecore.impl.EParameterImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.ActionExtractor;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.ErrorExtractor;
import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.general.ModelProcessor;
import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.knowledge.QTable;

public class EcoreModelProcessor implements ModelProcessor {
	private Knowledge knowledge;
	private List<Error> errors;
	private ErrorExtractor errorExtractor;

	public EcoreModelProcessor(Knowledge knowledge) {
		this.knowledge = knowledge;
		errorExtractor = new EcoreErrorExtractor();
	}

	@Override
	public Set<Integer> initializeQTableForErrorsInModel(Model model) {
		if (model instanceof EcoreModel) {
			return initializeQTableForErrorsInModel((EcoreModel) model);
		} else {
			throw new IllegalArgumentException(
					"The method must be called with a model of type hvl.projectparmorel.ecore.EcoreModel");
		}
	}

	/**
	 * Goes through all the errors in the model, and if the error is not in the
	 * q-table is is added along with matching actions. Alternatively the error code
	 * is added to a set of unsupported errors.
	 * 
	 * @param model
	 * @param destinationURI
	 * @return a set of unsupported error codes that was not added to the Q-table
	 */
	private Set<Integer> initializeQTableForErrorsInModel(EcoreModel model) {
		errors = errorExtractor.extractErrorsFrom(model.getRepresentationCopy(), false);

		ActionExtractor actionExtractor = new EcoreActionExtractor(knowledge);
		List<Action> possibleActions = actionExtractor.extractActionsFor(errors);

		Set<Integer> unsupportedErrors = new HashSet<>();

		for (Error error : errors) {
			if (!knowledge.getQTable().containsErrorCode(error.getCode())
					&& !unsupportedErrors.contains(error.getCode())) {
				boolean actionForErrorFound = false;
				for (int i = 0; i < error.getContexts().size(); i++) {
					if (error.getContexts().get(i) != null) {
						for (Action action : possibleActions) {
							if (isInvokable(error, error.getContexts().get(i).getClass(), action)) {
								Resource modelCopy = (Resource) model.getRepresentationCopy();
								List<Error> newErrors = tryApplyAction(error, action, modelCopy, i);
								if (newErrors != null) {
									if (!errorStillExists(newErrors, error)) {
										actionForErrorFound = true;
										Action newAction = new Action(action.getCode(), action.getMessage(),
												action.getMethod(), i);
										initializeQTableForAction(error, newAction);
									}
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
		if (model instanceof EcoreModel) {
			return tryApplyAction(error, action, (Resource) model.getRepresentation(), action.getHierarchy());
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
		EPackage ePackage = (EPackage) model.getContents().get(0);
		EObject object = (EObject) error.getContexts().get(hierarchy);

		if (object != null) {
			boolean success = false;
			for (int i = 0; i < ePackage.getEClassifiers().size() && !success; i++) {
				success = identifyObjectTypeAndApplyAction(error, action, object, ePackage.getEClassifiers().get(i));
			}
			List<Error> newErrors = errorExtractor.extractErrorsFrom(model, false);
			return newErrors;
		}
		return null;
	}

	/**
	 * Finds out what type of EMOF-class the object is, and converts it to that type
	 * before applying the correct action.
	 * 
	 * @param error
	 * @param action
	 * @param object
	 * @param eClassifier
	 * @return true if an action was successfully applied, false otherwise
	 */
	private boolean identifyObjectTypeAndApplyAction(Error error, Action action, EObject object,
			EClassifier eClassifier) {
		if (object.getClass() == EClassImpl.class && eClassifier.getClass() == EClassImpl.class) {
			return applyAction(eClassifier, error, action, object);
		}
		boolean success;
		if (eClassifier.getClass() == EClassImpl.class) {
			EClassImpl eClass = (EClassImpl) eClassifier;
			success = handleReferencesAndAttributes(error, action, object, eClass);
			if (success)
				return true;
			return handleOperations(error, action, object, eClass);
		} else if (isEnum(object) && eClassifier instanceof EEnumImpl) {
			return handleEnum(error, action, object, eClassifier);
		}
		return false;
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
		int numberOfErrorCodeOriginally = 0;

		for (Error e : errors) {
			if (e.getCode() == errorCode) {
				numberOfErrorCodeOriginally++;
			}
		}

		int numberOfErrorCodeNow = 0;
		for (Error e : newErrors) {
			if (e.getCode() == errorCode) {
				numberOfErrorCodeNow++;
			}
		}

		return numberOfErrorCodeNow >= numberOfErrorCodeOriginally;
	}

	/**
	 * Applies the action to the eObject
	 * 
	 * @param eObject
	 * @param error
	 * @param action
	 * @param eObject1
	 * @return true if action is applied, false otherwise
	 */
	private boolean applyAction(EObject eObject, Error error, Action action, EObject eObject1) {
		if (isSameElement(eObject1, eObject)) { // Check if element is the correct one to fix
			if (action.isDelete()) {
				return deleteClass(eObject, error);
			} else {
				if (error.getCode() == 4 && eObject.getClass() == EGenericTypeImpl.class) { // if needs to add type
																							// arguments
					addTypeArguments(error, action);
					return true;
				}
				if (isInvokable(error, eObject.getClass(), action)) {
					if (action.getMethod().getMethod().getParameterCount() > 0) {// if method has parameters
						applyActionsThatRequireParameters(eObject, error, action);
					} else {
						invokeMethod(action.getMethod().getMethod(), eObject);
						return true;
					}

				}
			}
		}
		return false;
	}

	/**
	 * Handles if the object is a reference or attributes
	 * 
	 * @param action
	 * @param error
	 * @param object
	 * @param eClass
	 * @return true if an action is successfully applied, false otherwise
	 */
	private boolean handleReferencesAndAttributes(Error error, Action action, EObject object, EClassImpl eClass) {
		boolean success = false;
		for (int i = 0; i < eClass.getEAllStructuralFeatures().size(); i++) { // iterate over attributes and references
			EStructuralFeature feature = eClass.getEAllStructuralFeatures().get(i);
			if (isReferenceOrAttribute(object)) {
				success = applyAction(feature, error, action, object);
			} else if (isGenericType(object) && feature instanceof EReferenceImpl) {
				EReferenceImpl eReference = (EReferenceImpl) feature;
				EGenericTypeImpl eGeneric = (EGenericTypeImpl) eReference.getEGenericType();
				if (eGeneric != null) {
					success = applyAction(eGeneric, error, action, object);
				}
			}
			if (success)
				return true;
		}
		return false;
	}

	/**
	 * Checks if the specified object is a reference or attribute
	 * 
	 * @param object
	 * @return true if object is an attribute or a reference
	 */
	private boolean isReferenceOrAttribute(EObject object) {
		return object.getClass() == EAttributeImpl.class || object.getClass() == EReferenceImpl.class;
	}

	/**
	 * Checks if the specified object is a structure
	 * 
	 * @param object
	 * @return true if the object is a structure
	 */
	private boolean isGenericType(EObject object) {
		return object.getClass() == EGenericTypeImpl.class;
	}

	/**
	 * Handles objects of type operations.
	 * 
	 * @param error
	 * @param action
	 * @param object
	 * @param eClass
	 * @return true if an action is successfully applied, false otherwise
	 */
	private boolean handleOperations(Error error, Action action, EObject object, EClassImpl eClass) {
		boolean success;
		if (isOperation(object)) {
			for (int i = 0; i < eClass.getEAllOperations().size(); i++) {
				success = applyAction(eClass.getEAllOperations().get(i), error, action, object);
				if (success)
					return true;
			}
		} else if (isTypeParameter(object)) {
			for (int i = 0; i < eClass.getETypeParameters().size(); i++) {
				success = applyAction(eClass.getETypeParameters().get(i), error, action, object);
				if (success)
					return true;
			}
		} else if (isParameter(object)) {
			for (int i = 0; i < eClass.getEAllOperations().size(); i++) {
				EOperationImpl eOperation = (EOperationImpl) eClass.getEAllOperations().get(i);
				for (int j = 0; j < eOperation.getEParameters().size(); j++) {
					success = applyAction(eOperation.getEParameters().get(j), error, action, object);
					if (success)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the specified object is a non-generic operation
	 * 
	 * @param object
	 * @return true if the object is an operation, false otherwise
	 */
	private boolean isOperation(EObject object) {
		return object.getClass() == EOperationImpl.class && object.getClass() != EGenericTypeImpl.class;
	}

	/**
	 * Checks if the specified object is a type parameter
	 * 
	 * @param object
	 * @return true if the object is a type parameter, false otherwise
	 */
	private boolean isTypeParameter(EObject object) {
		return object.getClass() == ETypeParameter.class;
	}

	/**
	 * Checks if the specified object is a parameter
	 * 
	 * @param object
	 * @return true if the object is a parameter, false otherwise
	 */
	private boolean isParameter(EObject object) {
		return object.getClass() == EParameterImpl.class;
	}

	/**
	 * Checks if the specified object is an enum
	 * 
	 * @param object
	 * @return true if the object is an enum
	 */
	private boolean isEnum(EObject object) {
		return object.getClass() == EEnumLiteralImpl.class;
	}

	/**
	 * Handles enum objects
	 * 
	 * @param error
	 * @param action
	 * @param object
	 * @param eClassifier of type org.eclipse.emf.ecore.impl.EEnumImpl
	 * @return true if an action is successfully applied, false otherwise
	 */
	private boolean handleEnum(Error error, Action action, EObject object, EClassifier eClassifier) {
		EEnumImpl eEnum = (EEnumImpl) eClassifier;
		for (int i = 0; i < eEnum.getELiterals().size(); i++) {
			boolean success;
			EEnumLiteralImpl enumLiteral = (EEnumLiteralImpl) eEnum.getELiterals().get(i);
			if (!object.toString().contains("null") && enumLiteral.toString() != null) {
				success = applyAction(enumLiteral, error, action, object);
				if (success)
					return true;
			} else {
				if (object.getClass() == EEnumLiteralImpl.class) {
					EEnumLiteralImpl enumObject = (EEnumLiteralImpl) object;
					if (enumLiteral == enumObject) {
						success = applyAction(enumLiteral, error, action, enumObject);
						if (success)
							return true;
					}
				}
			}
		}
		return false;
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

		if (!actionDirectory.containsActionForErrorAndContext(error.getCode(), contextId, action.getCode())) {
			action.setWeight(0);
			actionDirectory.setAction(error.getCode(), contextId, action);
		}
	}

	/**
	 * Applies actions that require parameters of different types.
	 * 
	 * @param eObject
	 * @param error
	 * @param action
	 * @return true if method was invoked, false otherwise.
	 */
	private boolean applyActionsThatRequireParameters(EObject eObject, Error error, Action action) {
		Object[] values = getDefaultValues(extractParameterTypes(action.getMethod().getMethod(), error));
		// if input needs a date
		if (values.length != 0 && eObject instanceof EAttributeImpl
				&& action.getMethod().getMethod().getName().contains("DefaultValue") && error.getCode() != 40
				&& ((ETypedElement) eObject).getEType() != null
				&& ((ETypedElement) eObject).getEType().toString().contains("Date")) {

			invokeMethod(action.getMethod().getMethod(), eObject, new Date());
			return true;
		} else {
			// if dealing with opposite references
			if (values.length == action.getMethod().getMethod().getParameterCount()) {
				if (error.getCode() == 14 && action.getMessage().contains("setEOpposite")
						&& eObject instanceof EReferenceImpl) {
					values[0] = findOpposite((EReferenceImpl) eObject, error);
				}

				try {
					invokeMethod(action.getMethod().getMethod(), eObject, values);

					if (error.getCode() == 40 && action.getCode() == 591449609) { // sometimes this action in that error
																					// is problematic
						EAttributeImpl attribute = (EAttributeImpl) eObject;
						if (!attribute.isSetEGenericType()) {
							EGenericType genericType = EcoreFactory.eINSTANCE.createEGenericType();
							genericType.setEClassifier(EcorePackage.Literals.ESTRING);
							attribute.setEGenericType(genericType);
						}
					}
					return true;
				} catch (java.lang.ClassCastException | java.lang.IllegalArgumentException exception) {
					// Catch some exception
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Deletes a class. If the class has any references pointing to it, these will
	 * also be deleted.
	 * 
	 * @param classToDelete
	 * @param error
	 * @return true if the class was successfully deleted
	 */
	private boolean deleteClass(EObject classToDelete, Error error) {
		if (classToDelete.getClass() == EClassImpl.class
				&& !isSameElement(classToDelete, (EObject) error.getContexts().get(0))) {
			for (EReference reference : ((EClassImpl) classToDelete).getEAllReferences()) {
				try {
					EReferenceImpl oppositeReference = (EReferenceImpl) invokeMethod(
							EReference.class.getMethod("getEOpposite"), reference);
					if (oppositeReference != null) {
						EcoreUtil.delete(oppositeReference, true);
					}
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				} catch (SecurityException e1) {
					e1.printStackTrace();
				}
			}
		}
		EcoreUtil.delete(classToDelete, true);
		return true;
	}

	/**
	 * Invokes the method with the provided parameter
	 * 
	 * @param method
	 * @param param
	 * @return the return value from the method
	 */
	private Object invokeMethod(Method method, Object param) {
		try {
			return method.invoke(param);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Invokes the method with the provided parameters
	 * 
	 * @param method
	 * @param param1
	 * @param param2
	 * @return the return value from the method
	 */
	private Object invokeMethod(Method method, Object param1, Object... param2) {
		try {
			return method.invoke(param1, param2);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Add type arguments to error location
	 * 
	 * @param error
	 * @param action
	 */
	private void addTypeArguments(Error error, Action action) {
		EGenericTypeImpl genericType = (EGenericTypeImpl) error.getContexts().get(0);
		genericType.getETypeArguments().add(genericType);
		action.setCode(88888);
		action.setMessage("getETypeArguments().add(genericType)");
	}

	/**
	 * extract types of parameters for the method
	 * 
	 * @param method
	 * @param error
	 * @return a list of parameter type names
	 */
	private List<String> extractParameterTypes(Method method, Error error) {
		List<String> argsClass = new ArrayList<String>();

		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			if (method.getName().contentEquals("setEClassifier") || method.getName().contains("SetEGenericType")
					|| (error.getCode() == 401 && method.getName().contentEquals("setEType"))) {
				argsClass.add(parameterTypes[i].getName() + "CLASS");
			} else {
				if (method.getName().contentEquals("setTransient")) {
					argsClass.add(parameterTypes[i].getName() + "TRUE");
				} else {
					argsClass.add(parameterTypes[i].getName());
				}
			}
		}
		return argsClass;
	}

	/**
	 * Finds the opposite of the reference
	 * 
	 * @param reference
	 * @param error
	 * @return the opposite of the reference
	 */
	private EReferenceImpl findOpposite(EReferenceImpl reference, Error error) {
		if (reference.getEOpposite() == null) { // if the initial ref is opp null
			for (int i = 0; i < error.getContexts().size(); i++) { // look in the rest of refs
				if (error.getContexts().get(i) != null) {
					EReferenceImpl eaux = (EReferenceImpl) error.getContexts().get(i);
					if (eaux.getEOpposite() == reference) { // if the opposite is the first ref
						return eaux; // the first ref needs this one
					}
				}
			}
		}
		return null;
	}

	/**
	 * Checks if both objects are the same
	 * 
	 * @param element1
	 * @param element2
	 * @return true if objects is the same, false otherwise
	 */
	private boolean isSameElement(EObject element1, EObject element2) {
		String one = element1.toString();
		String two = element2.toString();
		int index = one.indexOf("@");
		int index2 = two.indexOf("@");
		int index3 = one.indexOf(" ");
		int index4 = two.indexOf(" ");

		String a1, a2, b1, b2;
		if (index != -1 || index2 != -1) {
			a1 = one.substring(0, index);
			a2 = one.substring(index3);
			b1 = two.substring(0, index2);
			b2 = two.substring(index4);
			one = a1 + a2;
			two = b1 + b2;
		}

		if (one.contentEquals(two)) {
			if (element1.eContents().size() == element2.eContents().size()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Simulates type preferences
	 * 
	 * @param list
	 * @return a list of default values for arguments
	 */
	private Object[] getDefaultValues(List<String> list) {
		List<Object> values = new ArrayList<Object>();
		Random rand = new Random();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).contentEquals("int")) {
				values.add(1);
			}
			if (list.get(i).contentEquals("boolean")) {
				values.add(false);
			}
			if (list.get(i).contentEquals("booleanTRUE")) {
				values.add(true);
			}
			if (list.get(i).contains("String")) {
				values.add("placeholder" + rand.nextInt((999999 - 1) + 1) + 1);
			}
			if (list.get(i).contentEquals("org.eclipse.emf.ecore.EClassifier")) {
				values.add(EcorePackage.Literals.ESTRING);
			}
			if (list.get(i).contentEquals("org.eclipse.emf.ecore.EClassifierCLASS")) {
				values.add(EcorePackage.Literals.ECLASS);
			}
			if (list.get(i).contentEquals("org.eclipse.emf.common.notify.NotificationChain")) {
				values.add(new NotificationChainImpl());
			}
			if (list.get(i).contains("TypeParameter")) {
				values.add(EcoreFactory.eINSTANCE.createETypeParameter());
			}
			if (list.get(i).contains("Reference")) {
				values.add(EcorePackage.Literals.EREFERENCE__EREFERENCE_TYPE);
			}
		}
		Object[] val = new Object[values.size()];
		val = values.toArray(val);

		return val;
	}
}
