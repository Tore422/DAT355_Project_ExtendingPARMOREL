package no.hvl.projectparmorel.qlearning.ecore;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;

import no.hvl.projectparmorel.qlearning.Error;
import no.hvl.projectparmorel.qlearning.ErrorExtractor;
import no.hvl.projectparmorel.qlearning.ModelType;

public class EcoreErrorExtractor implements ErrorExtractor {

	protected Set<Integer> unsuportedErrorCodes;

	public EcoreErrorExtractor() {
		this.unsuportedErrorCodes = ModelType.ECORE.getUnsupportedErrorCodes();
	}

	@Override
	public List<Error> extractErrorsFrom(Object model, boolean includeUnsupported) {
		if (model instanceof Resource) {
			Resource modelAsResource = (Resource) model;
			return extractErrorsFrom(modelAsResource, includeUnsupported);
		}
		throw new IllegalArgumentException("The model has to be of type org.eclipse.emf.ecore.resource.Resource");
	}

	/**
	 * Extracts the errors from the provided model.
	 * 
	 * @param model
	 * @param includeUnsupported is a boolean that specifies whether or not to
	 *                           include the unsupported errors.
	 * @return a list of errors found in the model
	 */
	private List<Error> extractErrorsFrom(Resource model, boolean includeUnsupported) {
		List<Error> errors = new ArrayList<Error>();

		for (int packageNumber = 0; packageNumber < model.getContents().size(); packageNumber++) {
			Diagnostic diagnostic = validateModelPackage(model, packageNumber);
			if (diagnostic.getSeverity() != Diagnostic.OK) {
				for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
					Error error = getErrorFor(childDiagnostic, packageNumber);
					if (error != null) {
						if (includeUnsupported) {
							errors.add(error);
						} else if (!unsuportedErrorCodes.contains(error.getCode())) {
							errors.add(error);
						}
					}
				}
			}
		}

		return errors;
	}

	/**
	 * Validates the model
	 * 
	 * @param model
	 * @param packageNumber
	 * @return the diagnostic for the model
	 */
	private Diagnostic validateModelPackage(Resource model, int packageNumber) {
		EObject object = model.getContents().get(packageNumber);
		return Diagnostician.INSTANCE.validate(object);
	}

	/**
	 * Gets the error for the specified diagnostic
	 * 
	 * @param diagnostic
	 * @param subpackageNumber
	 * @return the error for the specified diagnostic
	 */
	private Error getErrorFor(Diagnostic diagnostic, int packageNumber) {
		if (isPackageOrTwoFeatures(diagnostic)) {
			return new Error(diagnostic.getCode(), diagnostic.getMessage(), diagnostic.getData(), packageNumber);
		} else {
			return getErrorFromErrorCode(diagnostic, packageNumber);
		}
	}

	/**
	 * Checks whether the diagnostic is a package or two features, or if it is not.
	 * 
	 * @param diagnostic
	 * @param subpackageNumber
	 * @return true if the diagnostic is a package or has two features, false
	 *         otherwise.
	 */
	private boolean isPackageOrTwoFeatures(Diagnostic diagnostic) {
		return diagnostic.getData().get(0).getClass() == EPackageImpl.class
				|| diagnostic.getMessage().contains("two features");
	}

	private Error getErrorFromErrorCode(Diagnostic diagnostic, int packageNumber) {
		switch (diagnostic.getCode()) {
		case 40: // The typed element must have a type
			return handleError40(diagnostic, packageNumber);
		case 44: // The name X is not well formed
			return handleError44(diagnostic, packageNumber);
		default:
			Error error = handleEReferenceImplError(diagnostic, packageNumber);
			if (error == null) {
				error = new Error(diagnostic.getCode(), diagnostic.getMessage(), diagnostic.getData(), packageNumber);
			}
			return error;
		}
	}

	/**
	 * Gets an error that fits if the error code is 40 (The typed element must have
	 * a type).
	 * 
	 * It sets the error code of the return to 401 if it contains a EReferenceImpl,
	 * and 40 otherwise.
	 * 
	 * @param diagnostic
	 * @throws IllegalArgumentException if the code of the diagnostic is not 40
	 * @return an error object with code 40 or 401
	 */
	private Error handleError40(Diagnostic diagnostic, int packageNumber) {
		if (diagnostic.getCode() != 40)
			throw new IllegalArgumentException("This method should only be called when diagnostic code is 40.");
		String code = String.valueOf(diagnostic.getCode());
		if (diagnostic.getData().get(0).getClass().toString().contains("EReferenceImpl")) {
			code = code + "1";
			return new Error(Integer.parseInt(code), diagnostic.getMessage(), diagnostic.getData(), packageNumber);
		} else {
			return new Error(diagnostic.getCode(), diagnostic.getMessage(), diagnostic.getData(), packageNumber);
		}
	}

	/**
	 * Gets an error that fits if the error code is 44 (The name X is not well
	 * formed).
	 * 
	 * The error code is 44 if the class is of EReferenceImpl.class. The error code
	 * is 441 if the class contains EClassImpl The error code is 442 if the class
	 * contains EOperation The error code is 443 if the class contains EAttribute
	 * The error code is 444 if the class contains ETypeParameterImpl The error code
	 * is 445 if the class contains EEnum
	 * 
	 * @param diagnostic
	 * @throws IllegalArgumentException if the code of the diagnostic is not 44
	 * @return an error with specified code
	 */
	private Error handleError44(Diagnostic diagnostic, int packageNumber) {
		if (diagnostic.getCode() != 44)
			throw new IllegalArgumentException("This method should only be called when diagnostic code is 44.");
		String s = String.valueOf(44);
		if (diagnostic.getData().get(0).getClass().toString().contains("EClassImpl")) {
			s = s + "1";
		}
		if (diagnostic.getData().get(0).getClass().toString().contains("EOperation")) {
			s = s + "2";
		}
		if (diagnostic.getData().get(0).getClass().toString().contains("EAttribute")) {
			s = s + "3";
		}
		if (diagnostic.getData().get(0).getClass().toString().contains("ETypeParameterImpl")) {
			s = s + "4";
			// if name null
		}
		if (diagnostic.getData().get(0).getClass().toString().contains("EEnum")) {
			s = s + "5";
			// if name null
		}
		Error error = handleEReferenceImplError(diagnostic, packageNumber);
		if (error == null) {
			error = new Error(Integer.parseInt(s), diagnostic.getMessage(), diagnostic.getData(), packageNumber);
		}
		return error;
	}

	/**
	 * Handles errors where the class is of type EReferenceImpl.class
	 * 
	 * @param diagnostic
	 * @return an Error for EReferenceImpl.class, null otherwise
	 */
	private Error handleEReferenceImplError(Diagnostic diagnostic, int packageNumber) {
		if (diagnostic.getData().get(0).getClass() == EReferenceImpl.class) {
			try {
				Error e = new Error(diagnostic.getCode(), diagnostic.getMessage(), diagnostic.getData(), packageNumber);
				EReferenceImpl era = (EReferenceImpl) EReference.class.getMethod("getEOpposite")
						.invoke(diagnostic.getData().get(0));
				if (era != null) {
					List<Object> contexts = new ArrayList<Object>(diagnostic.getData());
					contexts.add(0, era);
					e.setContexts(contexts);
				}
				return e;
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
}
