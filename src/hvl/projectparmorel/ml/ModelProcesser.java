package hvl.projectparmorel.ml;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.eclipse.emf.ecore.impl.EEnumLiteralImpl;
import org.eclipse.emf.ecore.impl.EGenericTypeImpl;
import org.eclipse.emf.ecore.impl.EOperationImpl;
import org.eclipse.emf.ecore.impl.EParameterImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.impl.ETypeParameterImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ModelProcesser {
	private ResourceSet resourceSet;
	private hvl.projectparmorel.knowledge.Knowledge knowledge;
	
	boolean invoked;
	
	public ModelProcesser(ResourceSet resourceSet, hvl.projectparmorel.knowledge.Knowledge knowledge) {
		this.resourceSet = resourceSet;
		this.knowledge = knowledge;
	}
	
	public Resource processModel(Resource model, URI destinationURI) {
		Resource modelCopy = resourceSet.createResource(destinationURI);
		modelCopy.getContents().addAll(EcoreUtil.copyAll(model.getContents()));

		ActionExtractor actionExtractor = new ActionExtractor(knowledge);
		List<Error> errors = ErrorExtractor.extractErrorsFrom(model);
		List<Action> possibleActions = actionExtractor.extractActionsFor(errors);

		invoked = false;
		
		for (Error error : errors) {
			if (!knowledge.getActionDirectory().containsErrorCode(error.getCode())) {
				for (int i = 0; i < error.getWhere().size(); i++) {
					for (Action action : possibleActions) {
						if (error.getWhere().get(i) != null) {
							if (isInvokable(error, error.getWhere().get(i).getClass(), action)) {
								actionMatcher(error, action, modelCopy, true, i);
//								if (invoked) {
//									auxModel.getContents().clear();
//									auxModel.getContents().addAll(EcoreUtil.copyAll(model.getContents()));
//								}
							}
						}
					}
				}
			}
		}
		return model; // why this original model?
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
					if (action.getSerializableMethod().getMethod() != null
							&& action.getSerializableMethod().getMethod().hashCode() == method.hashCode()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public List<Error> actionMatcher(Error error, Action action, Resource model, Boolean light, int hierarchy) {
		EPackage epa = (EPackage) model.getContents().get(0);
		EObject object = null;
		boolean found = true;
		boolean invoked = false;
		EEnumLiteralImpl ob = null;

		// if applicable either on father or son

		if (error.getWhere().get(hierarchy) != null) {
			object = (EObject) error.getWhere().get(hierarchy);
			found = true;
			if (object == null && error.getWhere().get(hierarchy).getClass() == EEnumLiteralImpl.class) {
				ob = (EEnumLiteralImpl) error.getWhere().get(hierarchy);
				object = null;
			}
		} else {
			found = false;
		}

		// If whereError == whereAction
		if (found) {
			for (int i = 0; i < epa.getEClassifiers().size() && !invoked; i++) {
				// if action is for class and if error is in that classifier
				// This if is inside because we have to enter inside the classifiers anyway
				if (error.getWhere().get(hierarchy).getClass() == EClassImpl.class
						&& epa.getEClassifiers().get(i).getClass() == EClassImpl.class) {
					EClassImpl ec = (EClassImpl) epa.getEClassifiers().get(i);
					applyAction(ec, error, action, object);
					if (invoked)
						break;
				} else {
					if (epa.getEClassifiers().get(i).getClass() == EClassImpl.class && !invoked) {
						EClassImpl ec = (EClassImpl) epa.getEClassifiers().get(i);
						// iterate over attrbs and references
						// checking it is a reference or attrib action before iterating
						for (int j = 0; j < ec.getEAllStructuralFeatures().size(); j++) {
							// if it is an attribute
							if (error.getWhere().get(hierarchy).getClass() == EAttributeImpl.class
									|| error.getWhere().get(hierarchy).getClass() == EReferenceImpl.class) {

								applyAction(ec.getEAllStructuralFeatures().get(j), error, action, object);
								if (invoked)
									break;
							} // check if coincide and is a structure
								// if structure
							else if (error.getWhere().get(hierarchy).getClass() == EGenericTypeImpl.class
									&& !invoked) {
								if (ec.getEAllStructuralFeatures().get(j) instanceof EReferenceImpl) {
									EReferenceImpl er = (EReferenceImpl) ec.getEAllStructuralFeatures().get(j);
									EGenericTypeImpl eg = (EGenericTypeImpl) er.getEGenericType();
									if (eg != null) {
										applyAction(eg, error, action, object);
										if (invoked)
											break;
									}
								}
							} // if it is a generic type
						} // for j
							// checking it is an operation before iterating

						if (error.getWhere().get(hierarchy).getClass() == EOperationImpl.class
								&& error.getWhere().get(hierarchy).getClass() != EGenericTypeImpl.class && !invoked) {
							for (int k = 0; k < ec.getEAllOperations().size(); k++) {
								applyAction(ec.getEAllOperations().get(k), error, action, object);
								if (invoked)
									break;
							} // for j
						} // if operation
						else if (error.getWhere().get(hierarchy).getClass() == ETypeParameterImpl.class && !invoked) {
							for (int h = 0; h < ec.getETypeParameters().size() && !invoked; h++) {
								applyAction(ec.getETypeParameters().get(h), error, action, object);
								if (invoked)
									break;
							}
						}

						else if (error.getWhere().get(hierarchy).getClass() == EParameterImpl.class && !invoked) {
							for (int h = 0; h < ec.getEAllOperations().size() && !invoked; h++) {
								EOperationImpl eo = (EOperationImpl) ec.getEAllOperations().get(h);

								for (int y = 0; y < eo.getEParameters().size(); y++) {
									applyAction(eo.getEParameters().get(y), error, action, object);
									if (invoked)
										break;
								}

							}
						}
					} // if class
					else if (error.getWhere().get(hierarchy).getClass() == EEnumLiteralImpl.class && !invoked) {
						EEnumImpl eu = (EEnumImpl) epa.getEClassifiers().get(i);
						for (int w = 0; w < eu.getELiterals().size() && !invoked; w++) {
							EEnumLiteralImpl auxe = (EEnumLiteralImpl) eu.getELiterals().get(w);
							if (!object.toString().contains("null") && auxe.toString() != null) {
								applyAction(auxe, error, action, object);
								if (invoked)
									break;
							} // o is not null
							else {
								if (auxe == ob) {
									applyAction(auxe, error, action, ob);
									if (invoked)
										break;
								}
							} // for literals
						} // enumliteralimpl
							// if it is an Enum
					} // else as not in a class
				} // for i
			}
		} // if action class and element coincidential

		List<Error> newErrors = null;

		if (found)
			newErrors = ErrorExtractor.extractErrorsFrom(model);

		if (light && found) {
			// check error was solved
//			if (!errorChecker(newErrors, error, hierarchy - 1)) {
//				Action n = new Action(action.getCode(), action.getMsg(), action.getSerializableMethod(), hierarchy, sons);
//				initializeQTableForAction(error, n);
//				repairs = true;
//			}
		}
		return newErrors;
	}
	
	
	private void applyAction(EObject eobj, Error e, Action a, EObject o) {
//		try {
//// Check if element is the correct one to fix
//			if (checkIfSameElement(o, eobj)) {
//				if (String.valueOf(a.getCode()).startsWith("9999")) {
//					// if we delete a class we should also delete its references so that we don't
//					// get dangling elements
//					if (eobj.getClass() == EClassImpl.class) {
//						if (!checkIfSameElement(eobj, (EObject) e.getWhere().get(0))) {
//							for (EReference ref : ((EClassImpl) eobj).getEAllReferences()) {
//								EReferenceImpl era = (EReferenceImpl) EReference.class.getMethod("getEOpposite")
//										.invoke(ref);
//								if (era != null) {
//									EcoreUtil.delete(era, true);
//									invoked = true;
//								}
//							}
//							EcoreUtil.delete(eobj, true);
//						}
//					} else {
//						EcoreUtil.delete(eobj, true);
//						invoked = true;
//						return;
//					}
//				} else {
//					// if needs to add type arguments
//					if (e.getCode() == 4 && eobj.getClass() == EGenericTypeImpl.class) {
//						EGenericTypeImpl eg = (EGenericTypeImpl) e.getWhere().get(0);
//						eg.getETypeArguments().add(eg);
//						invoked = true;
//						a.setCode(88888);
//						a.setMsg("getETypeArguments().add(eg)");
//						return;
//					}
//					if (isInvokable(e, eobj.getClass(), a)) {
//						if (a.getSerializableMethod().getMethod().getParameterCount() > 0) {
//							Object[] values = argsDefaults(argsTypeExtractor(a.getSerializableMethod().getMethod(), e));
//							// if input needs a date
//							if (values.length != 0 && eobj instanceof EAttributeImpl
//									&& a.getSerializableMethod().getMethod().getName().contains("DefaultValue")
//									&& e.getCode() != 40 && ((ETypedElement) eobj).getEType() != null
//									&& ((ETypedElement) eobj).getEType().toString().contains("Date")) {
//
//								a.getSerializableMethod().getMethod().invoke(eobj, date);
//								invoked = true;
//								return;
//							} else {
//								// if dealing with opposite references
//								if (values.length == a.getSerializableMethod().getMethod().getParameterCount()) {
//									if (e.getCode() == 14 && a.getMsg().contains("setEOpposite")
//											&& eobj instanceof EReferenceImpl) {
//										values[0] = findOutOpposite((EReferenceImpl) eobj, e);
//									}
//									try {
//										a.getSerializableMethod().getMethod().invoke(eobj, values);
//										// sometimes this action in that error is problematic
//										if (e.getCode() == 40 && a.getCode() == 591449609) {
//											EAttributeImpl ea = (EAttributeImpl) eobj;
//											if (!ea.isSetEGenericType()) {
//												EGenericType eg = EcoreFactory.eINSTANCE.createEGenericType();
//												eg.setEClassifier(EcorePackage.Literals.ESTRING);
//												ea.setEGenericType(eg);
//											}
//										}
//										invoked = true;
//										return;
//									} catch (java.lang.ClassCastException
//											| java.lang.IllegalArgumentException exception) {
//										// Catch NullPointerExceptions.
//									}
//									invoked = true;
//									return;
//								}
//							}
//						} // if method has parameters
//						else {
//							a.getSerializableMethod().getMethod().invoke(eobj);
//							invoked = true;
//							return;
//						}
//						// check get parameters
//					} // if
//				}
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
	}
	
	private boolean checkIfSameElement(EObject o, EObject b) {
		String one = o.toString();
		String two = b.toString();
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
			if (o.eContents().size() == b.eContents().size()) {
				return true;
			}
		}

		return false;
	}
}
