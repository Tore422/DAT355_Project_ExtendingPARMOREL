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
	private hvl.projectparmorel.knowledge.Knowledge knowledge;

	protected static int N_EPISODES = 25;
	protected static double randomfactor = 0.25;

	private List<Error> errorsToFix;

	private final double MIN_ALPHA = 0.06; // Learning rate
	private final double gamma = 1.0; // Eagerness - 0 looks in the near future, 1 looks in the distant future
	private int reward = 0;
	Date date = new Date(1993, 1, 31);
	int total_reward = 0;
	boolean repairs = false;
	public URI uri;
	List<Error> original = new ArrayList<Error>();
//	List<Integer> processed = new ArrayList<Integer>();
	public List<Integer> originalCodes = new ArrayList<Integer>();
	Map<Integer, double[]> Q = new HashMap<Integer, double[]>();
	Map<Integer, Integer> errorMap = new HashMap<Integer, Integer>();
	List<Sequence> solvingMap = new ArrayList<Sequence>();
	Map<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> tagMap = new HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>();
	public ResourceSet resourceSet = new ResourceSetImpl();

	int MAX_EPISODE_STEPS = 20;
	boolean done = false;
	boolean invoked = false;
	NotificationChain msgs = new NotificationChainImpl();
	public Resource myMetaModel;
	public static int user;

	static double factor = 0.0;
	Sequence sx;
	public static List<Integer> preferences = new ArrayList<Integer>();

	private int weightRewardShorterSequencesOfActions;
	private int weightRewardLongerSequencesOfActions;
	private int weightRewardRepairingHighInErrorHierarchies;
	private int weightRewardRepairingLowInErrorHierarchies;
	protected static int weightPunishDeletion;
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

		errorsToFix = new ArrayList<Error>();
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

	public Sequence getBestSeq() {
		return sx;
	}

	public void setBestSeq(Sequence sx) {
		this.sx = sx;
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
		if (Math.random() < randomfactor) {
			return knowledge.getActionDirectory().getRandomActionForError(err.getCode());
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

	public void modelFixer(Resource auxModel) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		QTable qTable = knowledge.getQTable();
		ActionExtractor actionExtractor = new ActionExtractor(knowledge);

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
		errorsToFix = ErrorExtractor.extractErrorsFrom(auxModel2);
		solvingMap.clear();
		original.clear();
		original.addAll(errorsToFix);
//		errorMap.clear();
//		errorMap = extractDuplicates(original);
		System.out.println("PREFERENCES: " + preferences.toString());
		// FILTER ACTIONS AND INITIALICES QTABLE
		ModelProcesser modelProcesser = new ModelProcesser(resourceSet, knowledge);
		modelProcesser.initializeQTableForErrorsInModel(auxModel2, uri);
		// START with initial model its errors and actions
		System.out.println(errorsToFix.toString());
		System.out.println("EPISODES: " + N_EPISODES);
		while (episode < N_EPISODES) {
			index = 0;
			state = errorsToFix.get(index);
			sizeBefore = errorsToFix.size();
			total_reward = 0;
			alpha = alphas[episode];
			end_reward = 0;
			step = 0;
			doni = false;
			Sequence s = new Sequence();
//			ExperienceMap experience = knowledge.getExperience();
			while (step < MAX_EPISODE_STEPS) {
				action = chooseActionHash(state);

				errorsToFix.clear();
				errorsToFix = modelProcesser.tryApplyActionAndUpdatedQTableOnSuccess(state, action, auxModel2, false, action.getHierarchy()); //removed subHirerarchy - effect?
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
					if ((sizeBefore - errorsToFix.size()) > 1) {
						reward = reward + (2 / 3 * weightRewardModificationOfTheOriginalModel
								* (sizeBefore - errorsToFix.size()));
						addTagMap(state, code, action, 6, (2 / 3 * weightRewardModificationOfTheOriginalModel
								* (sizeBefore - errorsToFix.size())));
					} else {
						if ((sizeBefore - errorsToFix.size()) != 0)
							reward = reward - weightRewardModificationOfTheOriginalModel;
						addTagMap(state, code, action, 6, -weightRewardModificationOfTheOriginalModel);
					}
				}
				// low modification
				if (preferences.contains(5)) {
					if ((sizeBefore - errorsToFix.size()) > 1) {
						reward = reward - (2 / 3 * weightPunishModificationOfTheOriginalModel
								* (sizeBefore - errorsToFix.size()));
						addTagMap(state, code, action, 5, -(2 / 3 * weightPunishModificationOfTheOriginalModel
								* (sizeBefore - errorsToFix.size())));

					} else {
						if ((sizeBefore - errorsToFix.size()) != 0)
							reward = reward + weightPunishModificationOfTheOriginalModel;
						addTagMap(state, code, action, 5, weightPunishModificationOfTheOriginalModel);
					}
				}

				if (errorsToFix.size() != 0) {
					next_state = errorsToFix.get(index);

					if (!qTable.containsErrorCode(next_state.getCode())) {
//					if (!processed.contains(next_state.getCode())
//							|| !experience.getqTable().containsKey(next_state.getCode())) {
						errorsToFix = ErrorExtractor.extractErrorsFrom(auxModel2);
						actionExtractor.extractActionsFor(errorsToFix);
						modelProcesser.initializeQTableForErrorsInModel(auxModel2, uri);
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

					next_state = errorsToFix.get(index);
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
					sizeBefore = errorsToFix.size();
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
			errorsToFix.clear();
			errorsToFix.addAll(original);

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
