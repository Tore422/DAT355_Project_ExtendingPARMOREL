package hvl.projectparmorel.ml;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import hvl.projectparmorel.knowledge.ActionDirectory;
import hvl.projectparmorel.knowledge.QTable;

/**
 * Western Norway University of Applied Sciences Bergen, Norway
 * 
 * @author Angela Barriga Rodriguez abar@hvl.no
 * @author Magnus Marthinsen
 */
public class QLearning {
//	private Knowledge knowledge;
	private hvl.projectparmorel.knowledge.Knowledge knowledge;

	protected static int N_EPISODES = 25;
	protected static double randomfactor = 0.25;

	private final double MIN_ALPHA = 0.06; // Learning rate
	private final double gamma = 1.0; // Eagerness - 0 looks in the near future, 1 looks in the distant future
	private int reward = 0;
	Map<Integer, Action> actionsReturned = new HashMap<Integer, Action>();
	Date date = new Date(1993, 1, 31);
	int total_reward = 0;
	boolean repairs = false;
	public URI uri;
	List<Error> original = new ArrayList<Error>();
	List<Integer> processed = new ArrayList<Integer>();
	public List<Integer> originalCodes = new ArrayList<Integer>();
	Map<Integer, double[]> Q = new HashMap<Integer, double[]>();
	Map<Integer, Integer> errorMap = new HashMap<Integer, Integer>();
	List<Sequence> solvingMap = new ArrayList<Sequence>();
	Map<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> tagMap = new HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>();
	static List<Action> actionsFound = new ArrayList<Action>();
	public ResourceSet resourceSet = new ResourceSetImpl();

	int MAX_EPISODE_STEPS = 20;
	boolean done = false;
	boolean invoked = false;
	public List<Error> nuQueue = new ArrayList<Error>();
	NotificationChain msgs = new NotificationChainImpl();
	public Resource myMetaModel;
	public static int user;

//	static Experience newXp = new Experience();

	static double factor = 0.0;
	Sequence sx;
	public static List<Integer> preferences = new ArrayList<Integer>();

	private int weightRewardShorterSequencesOfActions;
	private int weightRewardLongerSequencesOfActions;
	private int weightRewardRepairingHighInErrorHierarchies;
	private int weightRewardRepairingLowInErrorHierarchies;
	private int weightPunishDeletion;
	private int weightPunishModificationOfTheOriginalModel;
	private int weightRewardModificationOfTheOriginalModel;

	public QLearning() {
		Preferences prefs = new Preferences();
		knowledge = new hvl.projectparmorel.knowledge.Knowledge(); // preferences);
		weightRewardShorterSequencesOfActions = prefs.getWeightRewardShorterSequencesOfActions();
		weightRewardLongerSequencesOfActions = prefs.getWeightRewardLongerSequencesOfActions();
		weightRewardRepairingHighInErrorHierarchies = prefs.getWeightRewardRepairingHighInErrorHierarchies();
		weightRewardRepairingLowInErrorHierarchies = prefs.getWeightRewardRepairingHighInErrorHierarchies();
		weightPunishDeletion = prefs.getWeightPunishDeletion();
		weightPunishModificationOfTheOriginalModel = prefs.getWeightPunishModificationOfTheOriginalModel();
		weightRewardModificationOfTheOriginalModel = prefs.getWeightRewardModificationOfTheOriginalModel();
		prefs.saveToFile();
	}

//	/**
//	 * Saves the knowledge
//	 */
//	public void saveKnowledge() {
//		knowledge.save();
//	}

	public static double[] linspace(double min, double max, int points) {
		double[] d = new double[points];
		for (int i = 0; i < points; i++) {
			d[i] = min + i * (max - min) / (points - 1);
		}
		return d;
	}

	double[] alphas = linspace(1.0, MIN_ALPHA, N_EPISODES);

//	public static Experience getNewXp() {
//		return newXp;
//	}

	public Sequence getBestSeq() {
		return sx;
	}

	public void setBestSeq(Sequence sx) {
		this.sx = sx;
	}

	boolean isInvokable(Error e, Class<? extends Object> class1, Action a) {
		boolean yes = false;

		Method[] methods = class1.getMethods();
		if (String.valueOf(a.getCode()).startsWith("9999")) {
			yes = true;
		} else {
			if (String.valueOf(a.getCode()).startsWith("888") && e.getCode() == 4 && class1 != EClassImpl.class) {
				yes = true;
			} else {
				for (Method method : methods) {
					if (a.getSerializableMethod().getMethod() != null) {
						if (a.getSerializableMethod().getMethod().hashCode() == method.hashCode()) {
							yes = true;
							break;
						}
					}
				} // for
			}
		}
		return yes;

	}

	Resource processModel(Resource auxModel2) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		Resource auxModel = resourceSet.createResource(uri);
		auxModel.getContents().addAll(EcoreUtil.copyAll(auxModel2.getContents()));
		List<Action> aux = actionsFound;
//		ExperienceMap experience = knowledge.getExperience();

		for (int x = 0; x < nuQueue.size(); x++) {
			Error err = nuQueue.get(x);
			if (!knowledge.getActionDirectory().containsErrorCode(err.getCode())) {
//			if (!experience.getActionsDictionary().containsKey(err.getCode())) {
				for (int j = 0; j < err.getWhere().size(); j++) {
					// if package, look for origin in children
					for (int i = 0; i < aux.size(); i++) {
						if (err.getWhere().get(j) != null) {
							if (isInvokable(err, err.getWhere().get(j).getClass(), aux.get(i))) {
								if (err.getWhere().get(j).getClass() == EPackageImpl.class) {
									for (int h = 0; h < err.getSons(); h++) {
										actionMatcher(err, aux.get(i), auxModel, true, j + 1, h);
										if (invoked) {
											// if action applied reset model and go for next error
											// update model to keep working on it
											auxModel.getContents().clear();
											auxModel.getContents().addAll(EcoreUtil.copyAll(auxModel2.getContents()));
											if (repairs)
												err = nuQueue.get(x);
										}
									}

								} // if package
								else {
									actionMatcher(err, aux.get(i), auxModel, true, j + 1, -1);
									if (invoked) {

										auxModel.getContents().clear();
										auxModel.getContents().addAll(EcoreUtil.copyAll(auxModel2.getContents()));

										if (repairs)
											err = nuQueue.get(x);
									}
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
				if (!knowledge.getQTable().containsErrorCode(child.getCode())) {
//				if (!knowledge.getExperience().getqTable().containsKey(child.getCode())) {
					return true;
				}
			}
		}
		return false;
	}

	boolean checkIfSameElement(EObject o, EObject b) {
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

	Action chooseActionHash(Error err) {
		Action a = new Action();
		int count = 0;
		Integer val = 0;
		Integer act = 0;
//		ExperienceMap experience = knowledge.getExperience();
		if (Math.random() < randomfactor) {
			return knowledge.getActionDirectory().getRandomActionForError(err.getCode());

//			boolean set = false;
//				// how many error locations
////				int x = new Random().nextInt(experience.getActionsDictionary().get(err.getCode()).size());
//				int x = new Random().nextInt(knowledge.getActionDirectory().getNumberOfContextsForError(err.getCode()));
//				for (Integer key : experience.getActionsDictionary().get(err.getCode()).keySet()) {
//					if (count == x) {
//						val = key;
//						break;
//					}
//					count++;
//				}
//				// how many actions in that location
//				int y = new Random().nextInt(experience.getActionsDictionary().get(err.getCode()).get(val).size());
//				count = 0;
//				for (Integer key2 : experience.getActionsDictionary().get(err.getCode()).get(val).keySet()) {
//					if (count == y) {
//						act = key2;
//						break;
//					}
//					count++;
//				}
//				a = experience.getActionsDictionary().get(err.getCode()).get(val).get(act).getAction();

		} else {
			return knowledge.getOptimalActionForErrorCode(err.getCode());
		}
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

	void applyAction(EObject eobj, Error e, Action a, EObject o) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		// Check if element is the correct one to fix
		if (checkIfSameElement(o, eobj)) {
			if (String.valueOf(a.getCode()).startsWith("9999")) {
				// if we delete a class we should also delete its references so that we don't
				// get dangling elements
				if (eobj.getClass() == EClassImpl.class) {
					if (!checkIfSameElement(eobj, (EObject) e.getWhere().get(0))) {
						for (EReference ref : ((EClassImpl) eobj).getEAllReferences()) {
							EReferenceImpl era = (EReferenceImpl) EReference.class.getMethod("getEOpposite")
									.invoke(ref);
							if (era != null) {
								EcoreUtil.delete(era, true);
								invoked = true;
							}
						}
						EcoreUtil.delete(eobj, true);
					}
				} else {
					EcoreUtil.delete(eobj, true);
					invoked = true;
					return;
				}
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
				if (isInvokable(e, eobj.getClass(), a)) {
					if (a.getSerializableMethod().getMethod().getParameterCount() > 0) {
						Object[] values = argsDefaults(argsTypeExtractor(a.getSerializableMethod().getMethod(), e));
						// if input needs a date
						if (values.length != 0 && eobj instanceof EAttributeImpl
								&& a.getSerializableMethod().getMethod().getName().contains("DefaultValue")
								&& e.getCode() != 40 && ((ETypedElement) eobj).getEType() != null
								&& ((ETypedElement) eobj).getEType().toString().contains("Date")) {

							a.getSerializableMethod().getMethod().invoke(eobj, date);
							invoked = true;
							return;
						} else {
							// if dealing with opposite references
							if (values.length == a.getSerializableMethod().getMethod().getParameterCount()) {
								if (e.getCode() == 14 && a.getMsg().contains("setEOpposite")
										&& eobj instanceof EReferenceImpl) {
									values[0] = findOutOpposite((EReferenceImpl) eobj, e);
								}
								try {
									a.getSerializableMethod().getMethod().invoke(eobj, values);
									// sometimes this action in that error is problematic
									if (e.getCode() == 40 && a.getCode() == 591449609) {
										EAttributeImpl ea = (EAttributeImpl) eobj;
										if (!ea.isSetEGenericType()) {
											EGenericType eg = EcoreFactory.eINSTANCE.createEGenericType();
											eg.setEClassifier(EcorePackage.Literals.ESTRING);
											ea.setEGenericType(eg);
										}
									}
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
						a.getSerializableMethod().getMethod().invoke(eobj);
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
		EObject o = null;
		boolean found = true;
		invoked = false;
		repairs = false;
		EEnumLiteralImpl ob = null;

		// if applicable either on father or son

		if (e.getWhere().get(hierar - 1) != null) {
			o = (EObject) e.getWhere().get(hierar - 1);
			found = true;
			if (o == null && e.getWhere().get(hierar - 1).getClass() == EEnumLiteralImpl.class) {
				ob = (EEnumLiteralImpl) e.getWhere().get(hierar - 1);
				o = null;
			}
		} else {
			found = false;
		}

		// If whereError == whereAction
		if (found) {
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
							else if (e.getWhere().get(hierar - 1).getClass() == EGenericTypeImpl.class && !invoked) {
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
									applyAction(auxe, e, a, ob);
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
			newErrors = errorsExtractor(auxModel2);

		if (light && found) {
			// check error was solved
			if (!errorChecker(newErrors, e, hierar - 1)) {
				Action n = new Action(a.getCode(), a.getMsg(), a.getSerializableMethod(), hierar, sons);
				initializeQTableForAction(e, n);
				repairs = true;
			}
		}
		return newErrors;
	}

	/**
	 * Initializes the QTable for the specified action, setting an initial weight.
	 * 
	 * @param error
	 * @param action
	 */
	private void initializeQTableForAction(Error error, Action action) {
		QTable qTable = knowledge.getQTable();
		ActionDirectory actionDirectory = knowledge.getActionDirectory();

		int contextId = action.getContextId();

		if (!qTable.containsActionIdForErrorCodeAndContextId(error.getCode(), contextId, action.getCode())) {
			double weight = initializeWeightFor(action);
			qTable.setWeight(error.getCode(), contextId, action.getCode(), weight);

			if (!actionDirectory.containsActionForErrorAndContext(error.getCode(), contextId, action.getCode())) {
				actionDirectory.setAction(error.getCode(), contextId, action);
			}
		}
	}

	/**
	 * Initializes the weight for the given action
	 * 
	 * @param action
	 * @return initial weight
	 */
	private double initializeWeightFor(Action action) {
		double weight = 0.0;

		if (preferences.contains(4)) {
			if (action.getMsg().contains("delete")) {
				weight = -(double) weightPunishDeletion / 100;
			} else {
				weight = 0.0;
			}
		}

		if (action.getMsg().contains("get")) {
			weight = -10.0;
		} else {
			weight = 0.0;
		}

		return weight;
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
			errorMap = extractDuplicates(nuQueue);
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
							&& newErrors.get(i).getWhere().size() == e.getWhere().size()) {
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

	public void modelFixer(Resource auxModel) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		QTable qTable = knowledge.getQTable();

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
		int sizeBefore = 0;
		// Copy original broken model into an aux
		Resource auxModel2 = resourceSet.createResource(uri);
		auxModel2.getContents().add(EcoreUtil.copy(myMetaModel.getContents().get(0)));
		solvingMap.clear();
		original.clear();
		original.addAll(nuQueue);
		errorMap.clear();
		errorMap = extractDuplicates(original);
		System.out.println("PREFERENCES: " + preferences.toString());
		// FILTER ACTIONS AND INITIALICES QTABLE
		auxModel2 = processModel(auxModel2);
		// START with initial model its errors and actions
		System.out.println(nuQueue.toString());
		System.out.println("EPISODES: " + N_EPISODES);
		while (episode < N_EPISODES) {
			index = 0;
			state = nuQueue.get(index);
			sizeBefore = nuQueue.size();
			total_reward = 0;
			alpha = alphas[episode];
			end_reward = 0;
			step = 0;
			doni = false;
			Sequence s = new Sequence();
//			ExperienceMap experience = knowledge.getExperience();
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

				// check how the action has modified number of errors
				// high modification
				if (preferences.contains(6)) {
					if ((sizeBefore - nuQueue.size()) > 1) {
						reward = reward
								+ (2 / 3 * weightRewardModificationOfTheOriginalModel * (sizeBefore - nuQueue.size()));
						addTagMap(state, code, action, 6,
								(2 / 3 * weightRewardModificationOfTheOriginalModel * (sizeBefore - nuQueue.size())));
					} else {
						if ((sizeBefore - nuQueue.size()) != 0)
							reward = reward - weightRewardModificationOfTheOriginalModel;
						addTagMap(state, code, action, 6, -weightRewardModificationOfTheOriginalModel);
					}
				}
				// low modification
				if (preferences.contains(5)) {
					if ((sizeBefore - nuQueue.size()) > 1) {
						reward = reward
								- (2 / 3 * weightPunishModificationOfTheOriginalModel * (sizeBefore - nuQueue.size()));
						addTagMap(state, code, action, 5,
								-(2 / 3 * weightPunishModificationOfTheOriginalModel * (sizeBefore - nuQueue.size())));

					} else {
						if ((sizeBefore - nuQueue.size()) != 0)
							reward = reward + weightPunishModificationOfTheOriginalModel;
						addTagMap(state, code, action, 5, weightPunishModificationOfTheOriginalModel);
					}
				}

				if (nuQueue.size() != 0) {
					next_state = nuQueue.get(index);

					if (!processed.contains(next_state.getCode()) || !qTable.containsErrorCode(next_state.getCode())) {
//					if (!processed.contains(next_state.getCode())
//							|| !experience.getqTable().containsKey(next_state.getCode())) {
						nuQueue = errorsExtractor(auxModel2);
						actionsExtractor(nuQueue);
						auxModel2 = processModel(auxModel2);
					}
					// if new error introduced
					if (!originalCodes.contains(next_state.getCode())) {
						// System.out.println("NEW ERROR: " + next_state.toString());
						// high modification
						if (preferences.contains(6)) {
							reward = reward + 2 / 3 * weightRewardModificationOfTheOriginalModel;
						}
						// low modification
						if (preferences.contains(5)) {
							reward = reward - 2 / 3 * weightPunishModificationOfTheOriginalModel;
						}
					}

					next_state = nuQueue.get(index);
					Action a = knowledge.getOptimalActionForErrorCode(next_state.getCode());

					if (a.getSubHierarchy() != -1) {
						code2 = Integer.valueOf(String.valueOf(a.getHierarchy()) + String.valueOf(a.getSubHierarchy()));
					} else {
						code2 = a.getHierarchy();
					}
					double value = qTable.getWeight(state.getCode(), code, action.getCode())
							+ alpha * (reward + gamma * qTable.getWeight(next_state.getCode(), code2, a.getCode()))
							- qTable.getWeight(state.getCode(), code, action.getCode());

//					double value = experience.getqTable().get(state.getCode()).get(code).get(action.getCode())
//							+ alpha * (reward
//									+ gamma * experience.getqTable().get(next_state.getCode()).get(code2)
//											.get(a.getCode())
//									- experience.getqTable().get(state.getCode()).get(code).get(action.getCode()));

					qTable.setWeight(state.getCode(), code, action.getCode(), value);
//					experience.getqTable().get(state.getCode()).get(code).put(action.getCode(), value);
					state = next_state;
					sizeBefore = nuQueue.size();
				} // it has reached the end

				else {
					end_reward = 1;

					double value = qTable.getWeight(state.getCode(), code, action.getCode())
							+ alpha * (reward + gamma * end_reward)
							- qTable.getWeight(state.getCode(), code, action.getCode());

//					double value = experience.getqTable().get(state.getCode()).get(code).get(action.getCode())
//							+ alpha * (reward + gamma * end_reward)
//							- experience.getqTable().get(state.getCode()).get(code).get(action.getCode());
					qTable.setWeight(state.getCode(), code, action.getCode(), value);
//					experience.getqTable().get(state.getCode()).get(code).put(action.getCode(), value);
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
				// System.out.println(s.toString());
				solvingMap.add(s);
			} else {
				discarded++;
			}

			// RESET initial model and extract actions + errors
			auxModel2.getContents().clear();
			auxModel2.getContents().add(EcoreUtil.copy(myMetaModel.getContents().get(0)));
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
		setBestSeq(bestSequence(solvingMap));
		System.out.println(getBestSeq().toString());

		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// THIS SAVES THE REPAIRED MODEL
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if (getBestSeq().getSeq().size() != 0) {
			updateSequencesWeights(getBestSeq(), -1);
			sx.getModel().save(null);
		}

		System.out.println("********************************************************");

	}

	void updateSequencesWeights(Sequence s, int tag) {
//		ExperienceMap experience = knowledge.getExperience();
		QTable qTable = knowledge.getQTable();
		ActionDirectory actionDirectory = knowledge.getActionDirectory();
		int num;
		for (int i = 0; i < s.getSeq().size(); i++) {
			if (s.getSeq().get(i).getAction().getSubHierarchy() > -1) {
				num = Integer.valueOf(String.valueOf(s.getSeq().get(i).getAction().getHierarchy())
						+ String.valueOf(s.getSeq().get(i).getAction().getSubHierarchy()));
			} else {
				num = s.getSeq().get(i).getAction().getHierarchy();
			}
			int errorCode = s.getSeq().get(i).getError().getCode();
			int actionId = s.getSeq().get(i).getAction().getCode();
			double oldWeight = qTable.getWeight(errorCode, num, actionId);

			qTable.setWeight(errorCode, num, actionId, oldWeight + 300);
//			experience.getqTable().get(s.getSeq().get(i).getError().getCode()).get(num).put(
//					s.getSeq().get(i).getAction().getCode(),
//					experience.getqTable().get(s.getSeq().get(i).getError().getCode()).get(num)
//							.get(s.getSeq().get(i).getAction().getCode()) + 300);

			if (tag > -1) {

				if (!actionDirectory.getTagDictionaryForAction(errorCode, num, actionId).getTagDictionary()
						.containsKey(tag)) {
					actionDirectory.setTagValueInTagDictionary(errorCode, num, actionId, tag, 500);
//					actionDirectory.getTagDictionaryForAction(errorCode, num, actionId).getTagDictionary().put(tag, 500);
//				if (!experience.getActionsDictionary().get(s.getSeq().get(i).getError().getCode()).get(num)
//						.get(s.getSeq().get(i).getAction().getCode()).getTagsDictionary().containsKey(tag)) {

//					experience.getActionsDictionary().get(s.getSeq().get(i).getError().getCode()).get(num)
//							.get(s.getSeq().get(i).getAction().getCode()).getTagsDictionary().put(tag, 500);
				} else {
					int oldTagValue = actionDirectory.getTagDictionaryForAction(errorCode, num, actionId)
							.getTagDictionary().get(tag);
					actionDirectory.setTagValueInTagDictionary(errorCode, num, actionId, tag, oldTagValue + 500);
//					experience.getActionsDictionary().get(s.getSeq().get(i).getError().getCode()).get(num)
//							.get(s.getSeq().get(i).getAction().getCode()).getTagsDictionary().put(tag,
//									experience.getActionsDictionary().get(s.getSeq().get(i).getError().getCode())
//											.get(num).get(s.getSeq().get(i).getAction().getCode()).getTagsDictionary()
//											.get(tag) + 500);
				}
			}

			if (tagMap.containsKey(s.getSeq().get(i).getError().getCode())) {
				if (tagMap.get(s.getSeq().get(i).getError().getCode()).containsKey(num)) {
					if (tagMap.get(s.getSeq().get(i).getError().getCode()).get(num)
							.containsKey(s.getSeq().get(i).getAction().getCode())) {
						for (Integer key : tagMap.get(s.getSeq().get(i).getError().getCode()).get(num)
								.get(s.getSeq().get(i).getAction().getCode()).keySet()) {
							if (!actionDirectory.getTagDictionaryForAction(errorCode, num, actionId).getTagDictionary()
									.containsKey(key)) {
//							if (!experience.getActionsDictionary().get(s.getSeq().get(i).getError().getCode()).get(num)
//									.get(s.getSeq().get(i).getAction().getCode()).getTagsDictionary().containsKey(key)) {
								int newTagValue = tagMap.get(s.getSeq().get(i).getError().getCode()).get(num)
										.get(s.getSeq().get(i).getAction().getCode()).get(key);
								actionDirectory.setTagValueInTagDictionary(errorCode, num, actionId, key, newTagValue);

//								experience.getActionsDictionary().get(s.getSeq().get(i).getError().getCode()).get(num)
//										.get(s.getSeq().get(i).getAction().getCode()).getTagsDictionary().put(key, tagMap.get(s.getSeq().get(i).getError().getCode()).get(num).
//												get(s.getSeq().get(i).getAction().getCode()).get(key));
							} else {
								int newTagValue = actionDirectory.getTagDictionaryForAction(errorCode, num, actionId)
										.getTagDictionary().get(key)
										+ tagMap.get(s.getSeq().get(i).getError().getCode()).get(num)
												.get(s.getSeq().get(i).getAction().getCode()).get(key);
								actionDirectory.setTagValueInTagDictionary(errorCode, num, actionId, key, newTagValue);

//								experience.getActionsDictionary().get(s.getSeq().get(i).getError().getCode()).get(num)
//										.get(s.getSeq().get(i).getAction().getCode()).getTagsDictionary().put(key,
//												
//												experience.getActionsDictionary().get(s.getSeq().get(i).getError().getCode())
//														.get(num).get(s.getSeq().get(i).getAction().getCode()).getTagsDictionary()
//														.get(key) + 
//														tagMap.get(s.getSeq().get(i).getError().getCode()).get(num).
//														get(s.getSeq().get(i).getAction().getCode()).get(key));
							}
						}
					}
				}
			}
		}

	}

	void rewardSmallorBig(List<Sequence> sm) {
		int min = 9999;
		int max = 0;
		Sequence aux = null;

		if (preferences.contains(0)) {
			for (Sequence s : sm) {
				if (s.getSeq().size() < min && s.getWeight() > 0) {
					min = s.getSeq().size();
					aux = s;
				} else if (s.getSeq().size() == min) {
					if (s.getWeight() > aux.getWeight()) {
						aux = s;
					}
				}
			}
			aux.setWeight(aux.getWeight() + weightRewardShorterSequencesOfActions);
			updateSequencesWeights(aux, 0);
		}

		if (preferences.contains(1)) {
			for (Sequence s : sm) {
				if (s.getSeq().size() > max && s.getWeight() > 0) {
					max = s.getSeq().size();
					aux = s;
				} else if (s.getSeq().size() == max) {
					if (s.getWeight() > aux.getWeight()) {
						aux = s;
					}
				}
			}
			aux.setWeight(aux.getWeight() + weightRewardLongerSequencesOfActions);
			updateSequencesWeights(aux, 1);
		}
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
		double max = -1;
		rewardSmallorBig(sm);
		Sequence maxS = new Sequence();
		for (Sequence s : sm) {
			// normalize weights so that longer rewards dont get priority
			if (s.getWeight() > max) {
				max = s.getWeight();
				maxS = s;
			}
		}
		return maxS;
	}

	// Action rewards
	int rewardCalculator(Error state, Action action) {
		int reward = 0;
		int num;

		List<Integer> tagsFound = new ArrayList<Integer>();

		if (action.getSubHierarchy() > -1) {
			num = Integer.valueOf(String.valueOf(action.getHierarchy()) + String.valueOf(action.getSubHierarchy()));
		} else {
			num = action.getHierarchy();
		}

		if (preferences.contains(2)) {
			if (action.getHierarchy() == 1) {
				reward += weightRewardRepairingHighInErrorHierarchies;
				addTagMap(state, num, action, 2, weightRewardRepairingHighInErrorHierarchies);
			} else if (action.getHierarchy() == 2) {
				reward += weightRewardRepairingHighInErrorHierarchies * 2 / 3;
				addTagMap(state, num, action, 2, weightRewardRepairingHighInErrorHierarchies * 2 / 3);
			} else if (action.getHierarchy() > 2) {
				reward -= -74 / 100 * weightRewardRepairingHighInErrorHierarchies;
				addTagMap(state, num, action, 2, -74 / 100 * weightRewardRepairingHighInErrorHierarchies);
			}
		}
		if (preferences.contains(3)) {
			if (action.getHierarchy() == 1) {
				reward -= 74 / 100 * weightRewardRepairingLowInErrorHierarchies;
				addTagMap(state, num, action, 3, -74 / 100 * weightRewardRepairingLowInErrorHierarchies);
			}
			if (action.getHierarchy() == 2) {
				reward += weightRewardRepairingLowInErrorHierarchies * 2 / 3;
				addTagMap(state, num, action, 3, weightRewardRepairingLowInErrorHierarchies * 2 / 3);
			}
			if (action.getHierarchy() > 2) {
				reward += weightRewardRepairingLowInErrorHierarchies;
				addTagMap(state, num, action, 3, weightRewardRepairingLowInErrorHierarchies);
			}
		}

		if (preferences.contains(4)) {
			if (action.getMsg().contains("delete")) {
				reward -= weightPunishDeletion;
				addTagMap(state, num, action, 4, -weightPunishDeletion);
			} else {
				reward += weightPunishDeletion / 10;
				addTagMap(state, num, action, 4, weightPunishDeletion / 10);
			}
		}

		if (!preferences.contains(2) && !preferences.contains(3) && !preferences.contains(4)) {
			reward += 30;
		}

		return reward;
	}

	void addTagMap(Error state, int num, Action action, int tag, int r) {
		HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> hashaux = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
		HashMap<Integer, HashMap<Integer, Integer>> hashaux2 = new HashMap<Integer, HashMap<Integer, Integer>>();
		HashMap<Integer, Integer> hashaux3 = new HashMap<Integer, Integer>();

		if (!tagMap.containsKey(state.getCode())) {
			hashaux3.put(tag, r);
			hashaux2.put(action.getCode(), hashaux3);
			hashaux.put(num, hashaux2);
			tagMap.put(state.getCode(), hashaux);
		}
		if (!tagMap.get(state.getCode()).containsKey(num)) {
			hashaux3.put(tag, r);
			hashaux2.put(action.getCode(), hashaux3);
			tagMap.get(state.getCode()).put(num, hashaux2);
		}
		if (!tagMap.get(state.getCode()).get(num).containsKey(action.getCode())) {
			hashaux3.put(tag, r);
			tagMap.get(state.getCode()).get(num).put(action.getCode(), hashaux3);
		}
		if (!tagMap.get(state.getCode()).get(num).get(action.getCode()).containsKey(tag)) {
			tagMap.get(state.getCode()).get(num).get(action.getCode()).put(tag, r);
		} else {
			tagMap.get(state.getCode()).get(num).get(action.getCode()).put(tag,
					r + tagMap.get(state.getCode()).get(num).get(action.getCode()).get(tag));
		}
	}

	public Map<Integer, Action> actionsExtractor(List<Error> myErrors) {
		actionsFound.clear();
		actionsReturned.clear();
		// Each error

		for (int i = 0; i < myErrors.size(); i++) {
			if (!knowledge.getActionDirectory().containsErrorCode(myErrors.get(i).getCode())) {
//			if (!knowledge.getExperience().getActionsDictionary().containsKey(myErrors.get(i).getCode())) {
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
										&& !method.getName().contentEquals("eDynamicGet")
										&& !method.getName().contentEquals("dynamicSet")) {

									if (!actionsReturned.containsKey(method.hashCode())) {
										Action a = new Action(method.hashCode(), method.getName(),
												new SerializableMethod(method), j + 1, 0);
										// if the action was not already present
										actionsReturned.put(method.hashCode(), a);
										actionsFound.add(a);
									}
									// }
								}
							}
							if (!actionsReturned.containsKey(99999)) {
								Action a = new Action(99999, "delete", null, j + 1, 0);
								actionsReturned.put(99999, a);
								actionsFound.add(a);
							}
							//
						} // if not package
					} // is not null

				} // for error where structure
			} // for errors
			processed.add(myErrors.get(i).getCode());
		}
		return actionsReturned;

	}

	public List<Error> errorsExtractor(Resource myMM) throws IllegalAccessException, IllegalArgumentException,
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

	public static void createTags(int user) {
		switch (user) {
		// ECMFA paper preferences
		case 0:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 2, 4 }));
			break;
		// error hierarchy high, sequence short
		case 1:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 0, 2 }));
			break;
		// error hierarchy low, sequence long, high modification
		case 2:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 1, 3, 6 }));
			break;
		// avoid deletion
		case 3:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 4 }));
			break;
		// short sequence, low modification
		case 4:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 0, 4, 5 }));
			break;
		// long sequence, low modification, avoid deletion
		case 5:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 1, 5 }));
			break;
		// error hierarchy high
		case 6:
			preferences = new ArrayList<Integer>(Arrays.asList(new Integer[] { 2 }));
			break;
		}
	}

//	/**
//	 * Gets the knowledge
//	 * 
//	 * @return the knowledge
//	 */
//	public Knowledge getKnowledge() {
//		return knowledge;
//	}
}
