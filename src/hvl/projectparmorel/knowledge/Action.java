package hvl.projectparmorel.knowledge;

import hvl.projectparmorel.ml.Error;
import hvl.projectparmorel.ml.SerializableMethod;

/**
 * 
 * @author Angela Barriga Rodriguez - 2019, abar@hvl.no
 * @author Magnus Marthinsen
 * 
 * Western Norway University of Applied Sciences
 * Bergen - Norway
 */
public class Action implements Comparable<Action> {
	private PreferenceWeightMap preferenceMap;
	private int code;
	private String message;
	private SerializableMethod method;
	private int hierarchy;
	private int subHierarchy;
	private double weight;
	
	/**
	 * Creates an action with no parameters set.
	 */
	public Action() {
		preferenceMap = new PreferenceWeightMap();
	}
	
	/**
	 * Creates an action with all the parameters, except weight, set.
	 * 
	 * @param code
	 * @param message
	 * @param method
	 * @param hierarchy
	 * @param subHierarchy
	 */
	public Action(int code, String message, SerializableMethod method, int hierarchy, int subHierarchy) {
		this();
		this.code = code;
		this.message = message;
		this.method = method;
		this.hierarchy = hierarchy;
		this.subHierarchy = subHierarchy;
	}
	
	@Override
	public String toString() {
		return "Action" + code + ", msg=" + message + "." + System.getProperty("line.separator");
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Action) {
			Action otherAction = (Action) other;
			return otherAction.getCode() == code 
					&& otherAction.getMessage().equals(message)
					&& otherAction.getMethod().equals(method) 
					&& otherAction.getHierarchy() == hierarchy
					&& otherAction.getSubHierarchy() == subHierarchy;
		}
		return false;
	}

	/**
	 * Compares actions based on weights
	 */
	@Override
	public int compareTo(Action otherAction) {
		Double thisWeight = new Double(weight);
		Double otherWeight = new Double(otherAction.getWeight());
		return thisWeight.compareTo(otherWeight);
	}
	
	/**
	 * Checks if the action handles an error of type "The feature X of Y contains an
	 * unresolved proxy Z", by adding an argument to a generic type.
	 * 
	 * @param error
	 * @return true if the action handles it, false otherwise
	 */
	public boolean handlesMissingArgumentForGenericType(Error error) {
		return String.valueOf(code).startsWith("888") && error.getCode() == 4;
	}
	
	/**
	 * Checks if the action is a delete action
	 * 
	 * @return true if the action is a delete action, false otherwise
	 */
	public boolean isDelete() {
		return String.valueOf(code).startsWith("9999");
	}
	
	/**
	 * Gets the context id
	 * 
	 * @return the context ID
	 */
	public int getContextId() {
		if (subHierarchy > -1) {
			return Integer.valueOf(String.valueOf(hierarchy) + String.valueOf(subHierarchy));
		} else {
			return hierarchy;
		}
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public SerializableMethod getMethod() {
		return method;
	}

	public int getHierarchy() {
		return hierarchy;
	}

	public int getSubHierarchy() {
		return subHierarchy;
	}
	
	public void setCode(int code) {
		this.code = code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	protected PreferenceWeightMap getTagDictionary() {
		return preferenceMap;
	}

	public void savePreferenceWeights() {
		for(Integer preferenceId : preferenceMap.getAllPreferenceIds()) {
			preferenceMap.combineAndSavePreference(preferenceId);
		}
		
	}
}
