/*
 * Angela Barriga Rodriguez - 2019
 * abar@hvl.no
 * Western Norway University of Applied Sciences
 * Bergen - Norway
 */

package core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.notify.impl.NotificationChainImpl;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EDataTypeImpl;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.eclipse.emf.ecore.impl.EEnumLiteralImpl;
import org.eclipse.emf.ecore.impl.EGenericTypeImpl;
import org.eclipse.emf.ecore.impl.EOperationImpl;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.impl.EParameterImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.impl.ETypeParameterImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

public class QLearning {

	private final double MIN_ALPHA = 0.06; // Learning rate
	private final double gamma = 1.0; // Eagerness - 0 looks in the near future, 1 looks in the distant future
	private int reward = 0;
	Map<Integer, Action> actionsReturned = new HashMap<Integer, Action>();
	Date date = new Date(1993, 1, 31);
	int total_reward = 0;
	Map<Integer, HashMap<Integer, List<Action>>> actionSelected = new HashMap<Integer, HashMap<Integer, List<Action>>>();
	List<Action> actionSelected2 = new ArrayList<Action>();
	boolean repairs = false;
	URI uri;
	List<Error> original = new ArrayList<Error>();
	Resource auxiliar;
	Map<Integer, double[]> Q = new HashMap<Integer, double[]>();
	Map<Integer, Integer> errorMap = new HashMap<Integer, Integer>();
	Map<Integer, HashMap<Integer, Double>> nuQ = new HashMap<Integer, HashMap<Integer, Double>>();
	Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> nuQ2 = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>();
	List<Sequence> solvingMap = new ArrayList<Sequence>();
	static List<Action> actionsFound = new ArrayList<Action>();
	Map<Integer, String> errorsNumber = new HashMap<Integer, String>();
	ResourceSet resourceSet = new ResourceSetImpl();
	ResourceSet resourceSet2 = new ResourceSetImpl();
	private double eps = 0.10;
	int N_EPISODES = 15;
	int MAX_EPISODE_STEPS = 20;
	boolean done = false;
	boolean invoked = false;
	List<Error> nuQueue = new ArrayList<Error>();
	NotificationChain msgs = new NotificationChainImpl();
	List<String> errorsList = new ArrayList<String>();
	Resource myMetaModel;
	Resource myMetaModel2;

	public static double[] linspace(double min, double max, int points) {
		double[] d = new double[points];
		for (int i = 0; i < points; i++) {
			d[i] = min + i * (max - min) / (points - 1);
		}
		return d;
	}

	double[] alphas = linspace(1.0, MIN_ALPHA, N_EPISODES);

	void qTable(Error e, Action a) {
		int num;
		double weight = 0.0;
		HashMap<Integer, Double> d = new HashMap<Integer, Double>();
		HashMap<Integer, HashMap<Integer, Double>> dx = new HashMap<Integer, HashMap<Integer, Double>>();
		// Initialize all available actions weights to 0
		// for each error only available actions

		if (a.getMsg().contains("get") || a.getMsg().contains("delete")) {
			weight = -10.0;
		} else {
			weight = 0.0;
		}

		if (a.getSubHierarchy() > -1) {
			num = Integer.valueOf(String.valueOf(a.getHierarchy()) + String.valueOf(a.getSubHierarchy()));
		} else {
			num = a.getHierarchy();
		}

		// If error not present

		if (!nuQ2.containsKey(e.getCode())) {

			d.put(a.getCode(), weight);
			dx.put(num, d);

			nuQ2.put(e.getCode(), dx);

		}
		// Error not in Q table
		else {
			// Hierarchy already present
			if (nuQ2.get(e.getCode()).containsKey(num)) {
				// Action no already present
				if (!nuQ2.get(e.getCode()).get(num).containsKey(a.getCode())) {
					nuQ2.get(e.getCode()).get(num).put(a.getCode(), weight);
				}
			}
			// Hierar not in q
			else {
				d.put(a.getCode(), weight);
				nuQ2.get(e.getCode()).put(num, d);
			}

		}
	}

	// Returns key (action) with highest weight
	Action bestAction(Error err) {
		int sons = err.getSons();
		int code = 0;
		int maxi = 0;
		Double max = -99999.0;
		Integer pairKey = 0;
		Map<Integer, Double> mp = new HashMap<Integer, Double>();
		for (int i = 0; i < err.getWhere().size() - 1; i++) {
			if (sons != -1 && i == 0) {
				for (int j = 0; j < sons; j++) {
					code = Integer.valueOf(String.valueOf(i + 1) + String.valueOf(j));
					mp = nuQ2.get(err.getCode()).get(code);
					for (Entry<Integer, Double> entry : mp.entrySet()) {
						if ((Double) entry.getValue() > max) {
							max = (Double) entry.getValue();
							pairKey = (Integer) entry.getKey();
							maxi = code;
						}
					}
				}
			} // if sons
			else {
				if (nuQ2.get(err.getCode()).containsKey(i + 1)) {
					mp = nuQ2.get(err.getCode()).get(i + 1);
					for (Entry<Integer, Double> entry : mp.entrySet()) {
						if ((Double) entry.getValue() > max) {
							max = (Double) entry.getValue();
							pairKey = (Integer) entry.getKey();
							maxi = i + 1;
						}
					}
				}
			} // not sons
		} // for
		Action a = new Action();
		List<Action> as = actionSelected.get(err.getCode()).get(pairKey);
		for (int w = 0; w < as.size(); w++) {
			if (sons == -1 || as.get(w).getSubHierarchy() == -1) {
				if (as.get(w).getHierarchy() == maxi) {
					a = as.get(w);
					break;
				}
			} else {
				code = Integer.valueOf(
						String.valueOf(as.get(w).getHierarchy()) + String.valueOf(as.get(w).getSubHierarchy()));
				if (maxi == code) {
					a = as.get(w);
					break;
				}

			}
		}
		return a;

	}

	boolean isInvokable(Error e, int j, Action a) {
		boolean yes = false;

		if (j < e.getWhere().size()) {
			Class c = e.getWhere().get(j).getClass();
			Method[] methods = c.getMethods();
			if (String.valueOf(a.getCode()).startsWith("9999")) {
				yes = true;
			} else {
				if (String.valueOf(a.getCode()).startsWith("888") && e.getCode() == 4 && c != EClassImpl.class) {
					yes = true;
				} else {
					for (Method method : methods) {
						if (a.getMethod() != null) {
							if (a.getMethod().toString().contentEquals(method.toString())) {
								yes = true;
								break;
							}
						}
					} // for
				}
			}
		}
		return yes;

	}

	Resource processModel(Resource auxModel2) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		Resource auxModel = resourceSet.createResource(uri);
		auxModel.getContents().addAll(EcoreUtil.copyAll(auxModel2.getContents()));
		List<Action> aux = actionsFound;

		for (int x = 0; x < nuQueue.size(); x++) {
			Error err = nuQueue.get(x);
			for (int j = 0; j < err.getWhere().size(); j++) {
				// if package, look for origin in children
				for (int i = 0; i < aux.size(); i++) {
					if (err.getWhere().get(j) != null) {
						if (isInvokable(err, j, aux.get(i))) {
							if (err.getWhere().get(j).getClass() == EPackageImpl.class) {
								for (int h = 0; h < err.getSons(); h++) {
									actionMatcher(err, aux.get(i), auxModel, true, j + 1, h);
									if (invoked) {
										// if action applied reset model and go for next error
										// update model to keep working on it
										auxModel.getContents().clear();
										auxModel.getContents().addAll(EcoreUtil.copyAll(auxModel2.getContents()));

										err = nuQueue.get(x);
									}
								}

							} // if package
							else {
								actionMatcher(err, aux.get(i), auxModel, true, j + 1, -1);
								if (invoked) {

									auxModel.getContents().clear();
									auxModel.getContents().addAll(EcoreUtil.copyAll(auxModel2.getContents()));

									err = nuQueue.get(x);
								}
							}
						}
					}
				}
			}
		}

		return auxModel2;

	}

	boolean checkIfNewErrors(Resource r) {
		Diagnostic diagnostic = Diagnostician.INSTANCE.validate(r.getContents().get(0));
		if (diagnostic.getSeverity() != Diagnostic.OK) {
			for (Diagnostic child : diagnostic.getChildren()) {
				if (!nuQ2.containsKey(child.getCode())) {
					return true;
				}
			}
		}
		return false;
	}

	boolean checkIfSameElement(String a, String b) {
		int index = a.indexOf("@");
		int index2 = b.indexOf("@");
		int index3 = a.indexOf(" ");
		int index4 = b.indexOf(" ");

		String a1, a2, b1, b2;
		if (index != -1 || index2 != -1) {
			a1 = a.substring(0, index);
			a2 = a.substring(index3);
			b1 = b.substring(0, index2);
			b2 = b.substring(index4);
			a = a1 + a2;
			b = b1 + b2;
		}

		if (a.contentEquals(b)) {
			return true;
		}

		return false;
	}

	Action chooseActionHash(Error err) {
		Action a = new Action();
		if (Math.random() < eps) {
			boolean set = false;
			while (!set) {

				int x = new Random().nextInt(actionSelected.get(err.getCode()).size());
				int count = 0;
				Integer val = 0;
				int y = 0;
				for (Integer key : actionSelected.get(err.getCode()).keySet()) {
					if (count == x) {
						val = key;
						break;
					}
					count++;
				}

				if (actionSelected.get(err.getCode()).get(val).size() > 1) {
					y = new Random().nextInt(actionSelected.get(err.getCode()).get(val).size());
				}

				a = actionSelected.get(err.getCode()).get(val).get(y);
				set = true;

			}
		} else {
			a = bestAction(err);
		}

		return a;
	}

	// extract types of parameters given a method
	List<String> argsTypeExtractor(Method m, Error e) {
		List<String> argsClass = new ArrayList<String>();

		Class<?>[] p = m.getParameterTypes();
		for (int i = 0; i < p.length; i++) {
			if (m.getName().contentEquals("setEClassifier") || m.getName().contains("SetEGenericType")
					|| (e.getCode() == 401 && m.getName().contentEquals("setEType"))) {
				argsClass.add(p[i].getName() + "CLASS");
			} else {
				if (m.getName().contentEquals("setTransient")) {
					argsClass.add(p[i].getName() + "TRUE");
				} else {
					argsClass.add(p[i].getName());
				}
			}
		}
		return argsClass;
	}

	// Simulating type preferences
	Object[] argsDefaults(List<String> list) {
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

	void applyAction(EObject eobj, Error e, Action a, String o)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		//Check if element is the correct one to fix
		if (checkIfSameElement(o, eobj.toString())) {
			if (String.valueOf(a.getCode()).startsWith("9999")) {
				EcoreUtil.delete(eobj, true);
				invoked = true;
				return;
			} else {
				// if needs to add type arguments
				if (e.getCode() == 4 && eobj.getClass() == EGenericTypeImpl.class) {
					EGenericTypeImpl eg = (EGenericTypeImpl) e.getWhere().get(0);
					eg.getETypeArguments().add(eg);
					invoked = true;
					a.setCode(88888);
					a.setMsg("getETypeArguments().add(eg)");
					return;
				}
				if (isInvokable(e, a.getHierarchy() - 1, a)) {
					if (a.getMethod().getParameterCount() > 0) {
						Object[] values = argsDefaults(argsTypeExtractor(a.getMethod(), e));
						// if input needs a date
						if (values.length != 0 && eobj instanceof EAttributeImpl
								&& a.getMethod().getName().contains("DefaultValue") && e.getCode() != 40
								&& ((ETypedElement) eobj).getEType() != null
								&& ((ETypedElement) eobj).getEType().toString().contains("Date")) {

							a.getMethod().invoke(eobj, date);
							invoked = true;
							return;
						} else {
							// if dealing with opposite references
							if (values.length == a.getMethod().getParameterCount()) {
								if (e.getCode() == 14 && a.getMsg().contains("setEOpposite")
										&& eobj instanceof EReferenceImpl) {
									values[0] = findOutOpposite((EReferenceImpl) eobj, e);
								}
								try {
									a.getMethod().invoke(eobj, values);
									invoked = true;
									return;
								} catch (java.lang.ClassCastException | java.lang.IllegalArgumentException exception) {
									// Catch NullPointerExceptions.
								}
								invoked = true;
								return;
							}
						}
					} // if method has parameters
					else {
						a.getMethod().invoke(eobj);
						invoked = true;
						return;
					}
					// check get parameters
				} // if
			}
		}

	}

	List<Error> actionMatcher(Error e, Action a, Resource auxModel2, Boolean light, int hierar, int sons)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		EPackage epa = (EPackage) auxModel2.getContents().get(0);
		String o = " ";
		boolean found = true;
		invoked = false;
		repairs = false;
		EEnumLiteralImpl ob = null;

		// if applicable either on father or son
		if (e.getWhere().get(hierar - 1) != null) {
			o = e.getWhere().get(hierar - 1).toString();
			found = true;
			if (o == null && e.getWhere().get(hierar - 1).getClass() == EEnumLiteralImpl.class) {
				ob = (EEnumLiteralImpl) e.getWhere().get(hierar - 1);
				o = "null";
			}
		} else {
			found = false;
		}

		// If whereError == whereAction
		if (found) {
			if (e.getWhere().get(hierar - 1).getClass() == EPackageImpl.class && !invoked) {
				if (((EClassImpl) epa.getEClassifiers().get(sons)).getEAllReferences().size() != 0 && !invoked) {
					for (EReference ref : ((EClassImpl) epa.getEClassifiers().get(sons)).getEAllReferences()) {
						EReferenceImpl era = (EReferenceImpl) EReference.class.getMethod("getEOpposite").invoke(ref);
						if (era != null) {
							EcoreUtil.delete(era, true);
							invoked = true;
						}
					}
				}
				EcoreUtil.delete(epa.getEClassifiers().get(sons), true);
				invoked = true;

			} else {
				for (int i = 0; i < epa.getEClassifiers().size() && !invoked; i++) {
					// if action is for class and if error is in that classifier
					// This if is inside because we have to enter inside the classifiers anyway

					if (e.getWhere().get(hierar - 1).getClass() == EClassImpl.class
							&& epa.getEClassifiers().get(i).getClass() == EClassImpl.class) {
						EClassImpl ec = (EClassImpl) epa.getEClassifiers().get(i);
						applyAction(ec, e, a, o);
						if (invoked)
							break;
					} else {
						if (epa.getEClassifiers().get(i).getClass() == EClassImpl.class && !invoked) {
							EClassImpl ec = (EClassImpl) epa.getEClassifiers().get(i);
							// iterate over attrbs and references
							// checking it is a reference or attrib action before iterating
							for (int j = 0; j < ec.getEAllStructuralFeatures().size(); j++) {
								// if it is an attribute
								if (e.getWhere().get(hierar - 1).getClass() == EAttributeImpl.class
										|| e.getWhere().get(hierar - 1).getClass() == EReferenceImpl.class) {

									applyAction(ec.getEAllStructuralFeatures().get(j), e, a, o);
									if (invoked)
										break;
								} // check if coincide and is a structure
									// if structure
								else if (e.getWhere().get(hierar - 1).getClass() == EGenericTypeImpl.class
										&& !invoked) {
									if (ec.getEAllStructuralFeatures().get(j) instanceof EReferenceImpl) {
										EReferenceImpl er = (EReferenceImpl) ec.getEAllStructuralFeatures().get(j);
										EGenericTypeImpl eg = (EGenericTypeImpl) er.getEGenericType();
										if (eg != null) {
											applyAction(eg, e, a, o);
											if (invoked)
												break;
										}
									}
								} // if it is a generic type
							} // for j
								// checking it is an operation before iterating

							if (e.getWhere().get(hierar - 1).getClass() == EOperationImpl.class
									&& e.getWhere().get(hierar - 1).getClass() != EGenericTypeImpl.class && !invoked) {
								for (int k = 0; k < ec.getEAllOperations().size(); k++) {
									applyAction(ec.getEAllOperations().get(k), e, a, o);
									if (invoked)
										break;
								} // for j
							} // if operation
							else if (e.getWhere().get(hierar - 1).getClass() == ETypeParameterImpl.class && !invoked) {
								for (int h = 0; h < ec.getETypeParameters().size() && !invoked; h++) {
									applyAction(ec.getETypeParameters().get(h), e, a, o);
									if (invoked)
										break;
								}
							}

							else if (e.getWhere().get(hierar - 1).getClass() == EParameterImpl.class && !invoked) {
								for (int h = 0; h < ec.getEAllOperations().size() && !invoked; h++) {
									EOperationImpl eo = (EOperationImpl) ec.getEAllOperations().get(h);

									for (int y = 0; y < eo.getEParameters().size(); y++) {
										applyAction(eo.getEParameters().get(y), e, a, o);
										if (invoked)
											break;
									}

								}
							}
						} // if class
						else if (e.getWhere().get(hierar - 1).getClass() == EEnumLiteralImpl.class && !invoked) {
							EEnumImpl eu = (EEnumImpl) epa.getEClassifiers().get(i);
							for (int w = 0; w < eu.getELiterals().size() && !invoked; w++) {
								EEnumLiteralImpl auxe = (EEnumLiteralImpl) eu.getELiterals().get(w);
								if (!o.toString().contains("null") && auxe.toString() != null) {
									applyAction(auxe, e, a, o);
									if (invoked)
										break;
								} // o is not null
								else {
									if (auxe == ob) {
										applyAction(auxe, e, a, ob.toString());
										if (invoked)
											break;
									}
								}
							} // for literals
						} // enumliteralimpl
							// if it is an Enum
					} // else as not in a class
				} // for i
			}
		} // if action class and element coincidential

		List<Action> actionsR;
		List<Error> newErrors = null;
		if (found)
			newErrors = errorsExtractor(auxModel2);

		if (light && found) {
			// check error was solved
			if (!errorChecker(newErrors, e, hierar - 1)) {

				Action n = new Action(a.getCode(), a.getMsg(), a.getWhere(), a.getMethod(), hierar, sons);
				qTable(e, n);
				actionsR = new ArrayList<Action>();
				if (actionSelected.containsKey(e.getCode())) {

					if (!actionSelected.get(e.getCode()).containsKey(n.getCode())) {
						actionsR.add(n);
						actionSelected.get(e.getCode()).put(n.getCode(), actionsR);
					}
				} else {
					actionsR.add(n);
					actionSelected.put(e.getCode(), new HashMap<Integer, List<Action>>());
					actionSelected.get(e.getCode()).put(n.getCode(), actionsR);
				}

				actionSelected2.add(n);
				repairs = true;
			}
		}
		return newErrors;
	}

	EReferenceImpl findOutOpposite(EReferenceImpl er, Error e) {
		if (er.getEOpposite() == null) { // if the initial ref is opp null
			for (int i = 0; i < e.getWhere().size(); i++) { // look in the rest of refs
				if (e.getWhere().get(i) != null) { // if not null
					EReferenceImpl eaux = (EReferenceImpl) e.getWhere().get(i);
					if (eaux.getEOpposite() == er) { // if the opposite is the first ref
						return eaux; // the first ref needs this one
					}
				}
			}
		}
		return null;
	}

	boolean errorChecker(List<Error> newErrors, Error e, int index) {
		boolean found = false;
		if (errorMap.containsKey(e.getCode())) {
			extractDuplicates(newErrors);
			if (!errorMap.containsKey(e.getCode())) {
				found = false;
			} else {
				found = true;
			}
			errorMap = extractDuplicates(original);
		} else {
			for (int i = 0; i < newErrors.size(); i++) {

				if (e.getWhere().get(index).getClass() == EGenericTypeImpl.class) {
					EGenericTypeImpl eg = (EGenericTypeImpl) e.getWhere().get(index);
					if (eg.getETypeArguments().size() > 0) {
						found = false;
						break;
					}
				}
				if (newErrors.size() != 0) {

					if (newErrors.get(i).getCode() == e.getCode()
							&& newErrors.get(i).getWhere().size() == e.getWhere().size()
							&& newErrors.get(i).getSons() == e.getSons()) {
						found = true;
						break;

					}

				} else {
					break;
				}
			}
		}
		return found;
	}

	Map<Integer, Integer> extractDuplicates(List<Error> le) {
		errorMap.clear();
		for (int i = 0; i < le.size(); i++) {
			for (int j = 0; j < le.size(); j++) {

				if (le.get(i).getCode() == le.get(j).getCode()
						&& !le.get(i).getWhere().toString().contentEquals(le.get(j).getWhere().toString())) {
					errorMap.put(le.get(i).getCode(), 2);
				}
			}
		}
		return errorMap;

	}

	void modelFixer(Resource auxModel) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		int val;
		int discarded = 0;
		int episode = 0;
		int step = 0;
		int index = 0;
		Action action;
		double alpha;
		boolean doni = false;
		boolean nope = false;
		boolean alert = false;
		int code, code2;
		int end_reward;
		Error state, next_state;
		// Copy original broken model into an aux
		Resource auxModel2 = resourceSet.createResource(uri);
		auxModel2.getContents().addAll(EcoreUtil.copyAll(myMetaModel.getContents()));
		solvingMap.clear();
		original.clear();
		original.addAll(nuQueue);
		errorMap.clear();
		errorMap = extractDuplicates(original);
		// FILTER ACTIONS AND INITIALICES QTABLE
		auxModel2 = processModel(auxModel2);
		// START with initial model its errors and actions
		System.out.println("ACTIONS SELECTED:");
		System.out.println("Size:" + actionSelected.size());
		System.out.println(actionSelected.toString());
		System.out.println();

		while (episode < N_EPISODES) {
			index = 0;
			state = nuQueue.get(index);
			total_reward = 0;
			alpha = alphas[episode];
			end_reward = 0;
			step = 0;
			doni = false;
			Sequence s = new Sequence();

			while (step < MAX_EPISODE_STEPS) {
				action = chooseActionHash(state);

				nuQueue.clear();
				nuQueue = actionMatcher(state, action, auxModel2, false, action.getHierarchy(),
						action.getSubHierarchy());
				reward = rewardCalculator(state, action);
				// Insert stuff into sequence
				s.setId(episode);
				List<ErrorAction> ea = s.getSeq();
				ea.add(new ErrorAction(state, action));
				if ((state.getCode() == 401 || state.getCode() == 445 || state.getCode() == 27 || state.getCode() == 32)
						&& (action.getMsg().contentEquals("setEType") || action.getMsg().contentEquals("delete")
								|| action.getMsg().contentEquals("setName")
								|| action.getMsg().contentEquals("unsetEGenericType"))) {
					alert = true;
				}
				s.setSeq(ea);
				s.setU(uri);
				if (action.getSubHierarchy() != -1) {
					code = Integer
							.valueOf(String.valueOf(action.getHierarchy()) + String.valueOf(action.getSubHierarchy()));
				} else {
					code = action.getHierarchy();
				}

				if (nuQueue.size() != 0) {
					next_state = nuQueue.get(index);

					if (!nuQ2.containsKey(next_state.getCode())) {
						nuQueue = errorsExtractor(auxModel2);
						actionsExtractor(nuQueue);
						auxModel2 = processModel(auxModel2);
						System.out.println("NEW ERROR: " + next_state.toString());
						System.out.println("ACTIONS UPDATED:");
						System.out.println("Size:" + actionSelected.size());
						System.out.println(actionSelected.toString());
					}

					next_state = nuQueue.get(index);
					Action a = bestAction(next_state);

					if (a.getSubHierarchy() != -1) {
						code2 = Integer.valueOf(String.valueOf(a.getHierarchy()) + String.valueOf(a.getSubHierarchy()));
					} else {
						code2 = a.getHierarchy();
					}

					double value = nuQ2.get(state.getCode()).get(code).get(action.getCode())
							+ alpha * (reward + gamma * nuQ2.get(next_state.getCode()).get(code2).get(a.getCode())
									- nuQ2.get(state.getCode()).get(code).get(action.getCode()));
					nuQ2.get(state.getCode()).get(code).put(action.getCode(), value);
					state = next_state;
				} // it has reached the end
				else {
					end_reward = 1;
					double value = nuQ2.get(state.getCode()).get(code).get(action.getCode()) + alpha
							* (reward + gamma * end_reward - nuQ2.get(state.getCode()).get(code).get(action.getCode()));
					nuQ2.get(state.getCode()).get(code).put(action.getCode(), value);
					doni = true;
				}

				total_reward = total_reward + reward;

				if (doni) {
					break;
				}

				step++;
			}
			// add the whole sequence into list

			if (alert) {
				try {
					s.setModel(auxModel2);
				} catch (java.lang.NullPointerException exception) {
					// Catch NullPointerExceptions.
					nope = true;
				}
			} else {
				s.setModel(auxModel2);
			}

			if (s.getSeq().size() > 7) {
				val = loopChecker(s.getSeq());
				if (val > 1) {
					total_reward = total_reward - val * 1000;
				}
			}

			s.setWeight(total_reward);

			if (!nope && uniqueSequence(s)) {
				solvingMap.add(s);
			} else {
				discarded++;
			}

			// RESET initial model and extract actions + errors
			auxModel2.getContents().clear();
			auxModel2.getContents().addAll(EcoreUtil.copyAll(myMetaModel.getContents()));

			System.out.println("EPISODE " + episode + " TOTAL REWARD " + total_reward);
			nuQueue.clear();
			nuQueue.addAll(original);

			episode++;
			nope = false;
			alert = false;

		}
		System.out.println();

		System.out.println("-----------------ALL SEQUENCES FOUND-------------------");
		System.out.println("SIZE: " + solvingMap.size());
		// System.out.println(solvingMap.toString());
		System.out.println("DISCARDED SEQUENCES: " + discarded);
		System.out.println();
		System.out.println("********************************************************");
		System.out.println("--------::::B E S T   S E Q U E N C E   I S::::---------");
		Sequence sx = bestSequence(solvingMap);
		System.out.println(sx.toString());

		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// THIS SAVES THE REPAIRED MODEL
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 if (sx.getSeq().size() != 0) {
			 sx.getModel().save(null);
		 }

		System.out.println("********************************************************");

	}

	boolean uniqueSequence(Sequence s) {
		boolean check = true;
		int same = 0;
		for (Sequence seq : solvingMap) {
			if (seq.getWeight() == s.getWeight()) {
				for (ErrorAction ea : s.getSeq()) {
					for (ErrorAction ea2 : seq.getSeq()) {
						if (ea.equals(ea2)) {
							same++;
						}
					}
				} // for ea
					// if all elements in list are the same
				if (same == s.getSeq().size()) {
					check = false;
					break;
				}
			} // if weight
		} // for
		return check;
	}

	boolean checkWeight(Sequence s) {
		boolean check = false;
		for (Sequence seq : solvingMap) {
			if (seq.getWeight() == s.getWeight()) {
				check = true;
				break;
			}
		}
		return check;
	}

	int loopChecker(List<ErrorAction> ea) {
		List<Error> nums = new ArrayList<Error>();
		int value = 0;
		int index, index2 = 0;
		for (int i = 0; i < ea.size(); i++) {
			nums.add(ea.get(i).getError());
			if (nums.size() > 2) {
				if (ea.get(i).getError().getCode() == nums.get(i - 2).getCode()) {
					if (ea.get(i).getError().getWhere().get(0) == null) {
						index = 1;
					} else {
						index = 0;
					}
					if (nums.get(i - 2).getWhere().get(0) == null) {
						index2 = 1;
					} else {
						index2 = 0;
					}
					if (ea.get(i).getError().getWhere().get(index).getClass() == nums.get(i - 2).getWhere().get(index2)
							.getClass()) {
						value++;
					}
				}
			}
		}
		return value;
	}

	Sequence bestSequence(List<Sequence> sm) {
		double max = 0;
		Sequence maxS = new Sequence();
		for (Sequence s : sm) {
			if (s.getWeight() > max) {
				max = s.getWeight();
				maxS = s;
			}
		}
		return maxS;
	}

	int rewardCalculator(Error state, Action action) {
		// TODO these rewards are just for examples
		int reward;
		if (action.getMsg().contains("get") || action.getMsg().contains("delete")) {
			reward = -200;
		} else {
			if (action.getHierarchy() == 1) {
				reward = 400;
			} else if (action.getHierarchy() == 2) {
				reward = 300;
			} else if (action.getHierarchy() > 2) {
				reward = 100;
			} else {
				reward = -50;
			}
		}
		return reward;
	}

	Map<Integer, Action> actionsExtractor(List<Error> myErrors) {
		actionsFound.clear();
		actionsReturned.clear();
		// Each error

		for (int i = 0; i < myErrors.size(); i++) {
			if (!nuQ2.containsKey(myErrors.get(i).getCode())) {
				List<?> ca;
				// iterates over the whole structure of the error
				ca = (List<?>) myErrors.get(i).getWhere();
				// examples
				for (int j = 0; j < ca.size() - 1; j++) {
					if (ca.get(j) != null) {
						Class c = ca.get(j).getClass();
						if (c != EPackageImpl.class) {
							// For each method extract data
							Method[] methods = c.getMethods();
							for (Method method : methods) {
								if (!method.getName().startsWith("is") && !method.getName().startsWith("get")
										&& !method.getName().startsWith("to") && !method.getName().startsWith("e")
										&& !method.getName().contains("Get") && !method.getName().contains("Is")
										&& !method.getName().contentEquals("eDynamicIsSet")
										&& !method.getName().contentEquals("dynamicGet")
										&& !method.getName().contentEquals("hashCode")
										&& !method.getName().contentEquals("eVirtualIsSet")
										&& !method.getName().contentEquals("dynamicUnset")
										&& !method.getName().contentEquals("wait")
										&& !method.getName().contentEquals("eDynamicUnset")
										&& !method.getName().contentEquals("notify")
										&& !method.getName().contentEquals("notifyAll")
										&& !method.getName().contentEquals("eVirtualGet")
										&& !method.getName().contentEquals("eVirtualUnset")
										&& !method.getName().contentEquals("eDynamicGet")) {

									if (!actionsReturned.containsKey(method.hashCode())) {
										Action a = new Action(method.hashCode(), method.getName(), c, method, j + 1, 0);
										// if the action was not already present
										actionsReturned.put(method.hashCode(), a);
										actionsFound.add(a);
									}
									// }
								}
							}
							if (!actionsReturned.containsKey(99999)) {
								Action a = new Action(99999, "delete", c, null, j + 1, 0);
								actionsReturned.put(99999, a);
								actionsFound.add(a);
							}
							//
						} // if not package
						else {
							Method[] methods = c.getMethods();
							for (Method method : methods) {
								if (method.getName().contentEquals("getEClassifiers")) {

									Action a = new Action(method.hashCode(), method.getName(), c, method, 0, 0);
									// if the action was not already present
									actionsReturned.put(method.hashCode(), a);
									actionsFound.add(a);
								}
							}
						}
					} // is not null

				} // for error where structure
			} // for errors
		}
		return actionsReturned;

	}

	List<Error> errorsExtractor(Resource myMM) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		List<Error> errorsReturned = new ArrayList<Error>();
		// Validate
		int sons = 0;
		boolean introduced = false;
		Diagnostic diagnostic = Diagnostician.INSTANCE.validate(myMM.getContents().get(0));
		if (diagnostic.getSeverity() != Diagnostic.OK) {
			for (Diagnostic child : diagnostic.getChildren()) {
				// Each error found
				// numeric code for error, msg, and location within MM
				if (child.getCode() != 1) {
					if (child.getData().get(0).getClass() == EPackageImpl.class
							|| child.getMessage().contains("two features")) {
						for (int i = 1; i < child.getData().size() - 1; i++) {
							if (child.getData().get(i).toString().contains("Class")) {
								sons++;
							}
						}

						Error e = new Error(child.getCode(), child.getMessage(), child.getData(), sons);
						errorsReturned.add(e);
					} // if package or two features error
					else {
						if (child.getCode() == 40) { // if name null
							String s = String.valueOf(child.getCode());
							if (child.getData().get(0).getClass().toString().contains("EReferenceImpl")) {
								s = s + "1";
								Error e = new Error(Integer.parseInt(s), child.getMessage(), child.getData(), -1);
								errorsReturned.add(e);
								introduced = true;
							} else {
								Error e = new Error(child.getCode(), child.getMessage(), child.getData(), -1);
								errorsReturned.add(e);
								introduced = true;
							}
						} else {
							if (child.getCode() == 44) {
								String s = String.valueOf(44);
								if (child.getData().get(0).getClass().toString().contains("EClassImpl")) {
									s = s + "1";
								}
								if (child.getData().get(0).getClass().toString().contains("EOperation")) {
									s = s + "2";
								}
								if (child.getData().get(0).getClass().toString().contains("EAttribute")) {
									s = s + "3";
								}
								if (child.getData().get(0).getClass().toString().contains("ETypeParameterImpl")) {
									s = s + "4";
									// if name null
								}
								if (child.getData().get(0).getClass().toString().contains("EEnum")) {
									s = s + "5";
									// if name null
								}
								if (child.getData().get(0).getClass() == EReferenceImpl.class) {
									EReferenceImpl era = (EReferenceImpl) EReference.class.getMethod("getEOpposite")
											.invoke(child.getData().get(0));
									List<Object> L = new ArrayList<Object>(child.getData());
									Error e = new Error(child.getCode(), child.getMessage(), child.getData(), -1);
									if (era != null) {
										Object o = new Object();
										o = era;
										L.add(0, o);
										e.setWhere(L);
									}
									errorsReturned.add(e);
									introduced = true;
								} else {
									Error e = new Error(Integer.parseInt(s), child.getMessage(), child.getData(), -1);
									errorsReturned.add(e);
									introduced = true;
								}
							} // if 44

							else {
								if (!introduced) {
									if (child.getData().get(0).getClass() == EReferenceImpl.class) {
										EReferenceImpl era = (EReferenceImpl) EReference.class.getMethod("getEOpposite")
												.invoke(child.getData().get(0));
										List<Object> L = new ArrayList<Object>(child.getData());
										Error e = new Error(child.getCode(), child.getMessage(), child.getData(), -1);
										if (era != null) {
											Object o = new Object();
											o = era;
											L.add(0, o);
											e.setWhere(L);
										}
										errorsReturned.add(e);
									} else {
										Error e = new Error(child.getCode(), child.getMessage(), child.getData(), -1);
										errorsReturned.add(e);
									}
								}
							}
						}
					}
				}
				introduced = false;
			}
		}
		return errorsReturned;

	}

	public static void copyFile(File from, File to) throws IOException {
		Files.copy(from.toPath(), to.toPath());
	}

	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException, NoSuchMethodException, SecurityException {

		// JFileChooser chooser = new JFileChooser();
		// chooser.setMultiSelectionEnabled(true);
		// chooser.showOpenDialog((Frame) null);
		// File[] files = chooser.getSelectedFiles();

		long startTimeT = System.currentTimeMillis();
		long endTimeT = 0;
		String root = "././tofix/";
		String root2 = "././fixed/";

		File folder = new File(root);
		File[] listOfFiles = folder.listFiles();

		QLearning ql = new QLearning();

		for (int i = 0; i < listOfFiles.length; i++) {
		//invert mutant order	
		// for (int i = listOfFiles.length-1; i >=0; i--) {

			File dest = new File(root2 + listOfFiles[i].getName());
			long startTime = System.currentTimeMillis();
			long endTime = 0;
			System.out.println("----------------------------------------------------------------------");
			System.out.println("----------------------------------------------------------------------");
			System.out.println("STARTING WITH MODEL - " + i + ": " + root + listOfFiles[i].getName());

			// Copy original file
			copyFile(listOfFiles[i], dest);

			String file = root2 + listOfFiles[i].getName();

			ql.uri = URI.createFileURI(file);
			ql.resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
					new EcoreResourceFactoryImpl());
			ql.myMetaModel = ql.resourceSet.getResource(ql.uri, true);

			Resource auxModel = ql.resourceSet.createResource(ql.uri);
			auxModel.getContents().addAll(EcoreUtil.copyAll(ql.myMetaModel.getContents()));

			EPackage epa = (EPackage) auxModel.getContents().get(0);
			System.out.println("Num. Classes: " + epa.getEClassifiers().size());

			int enums = 0;
			int refs = 0;
			int attribs = 0;
			int ops = 0;
			int datatypes = 0;

			for (int j = 0; j < epa.getEClassifiers().size(); j++) {
				if (epa.getEClassifiers().get(j).getClass() == EEnumImpl.class) {
					enums++;
				} else {
					if (epa.getEClassifiers().get(j).getClass() == EDataTypeImpl.class) {
						datatypes++;
					} else {
						refs = refs + ((EClassImpl) epa.getEClassifiers().get(j)).getEAllReferences().size();
						attribs = attribs + ((EClassImpl) epa.getEClassifiers().get(j)).getEAllAttributes().size();
						ops = ops + ((EClassImpl) epa.getEClassifiers().get(j)).getEAllOperations().size();
					}
				}
			}

			System.out.println("Num. References: " + refs);
			System.out.println("Num. Attributes: " + attribs);
			System.out.println("Num. Operations: " + ops);
			System.out.println("Num. Enums: " + enums);
			System.out.println("Num. DataTypes: " + datatypes);
			System.out.println("----------------------------------------------------------------------");
			System.out.println("----------------------------------------------------------------------");

			List<Error> errors = ql.errorsExtractor(auxModel);
			ql.nuQueue = errors;

			System.out.println("INITIAL ERRORS:");
			System.out.println(errors.toString());
			System.out.println("Size= " + errors.size());
			System.out.println();

			ql.actionsExtractor(errors);

			ql.modelFixer(auxModel);

			endTime = System.currentTimeMillis();
			long timeneeded = (endTime - startTime);
			System.out.println("TOTAL TIME: " + timeneeded);
		}

		System.out.println("COMPLETELY FINISHED!!!!!!");
		endTimeT = System.currentTimeMillis();
		long timeneededT = (endTimeT - startTimeT);
		System.out.println("TOTAL EXECUTION TIME: " + timeneededT);
		System.exit(0);

	}
}
