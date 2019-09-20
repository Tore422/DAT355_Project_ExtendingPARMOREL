package hvl.projectparmorel.knowledge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import hvl.projectparmorel.ml.Error;
import hvl.projectparmorel.ml.SerializableMethod;

/**
 * 
 * @author Angela Barriga Rodriguez - 2019, abar@hvl.no
 * @author Magnus Marthinsen
 * 
 *         Western Norway University of Applied Sciences Bergen - Norway
 */
public class Action implements Comparable<Action>, Savable {
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
			return otherAction.getCode() == code && otherAction.getMessage().equals(message)
					&& otherAction.getMethod().equals(method) && otherAction.getHierarchy() == hierarchy
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
		for (Integer preferenceId : preferenceMap.getAllPreferenceIds()) {
			preferenceMap.combineAndSavePreference(preferenceId);
		}

	}

	@Override
	public void saveTo(Document document, Element action) {
		Element code = document.createElement("code");
		code.appendChild(document.createTextNode("" + this.code));
		action.appendChild(code);

		Element weight = document.createElement("weight");
		weight.appendChild(document.createTextNode("" + this.weight));
		action.appendChild(weight);

		Element message = document.createElement("message");
		message.appendChild(document.createTextNode("" + this.message));
		action.appendChild(message);

		Element hierarchy = document.createElement("hierarchy");
		hierarchy.appendChild(document.createTextNode("" + this.hierarchy));
		action.appendChild(hierarchy);

		Element subHierarchy = document.createElement("subHierarchy");
		subHierarchy.appendChild(document.createTextNode("" + this.subHierarchy));
		action.appendChild(subHierarchy);

		Element method = document.createElement("method");
		method.appendChild(document.createTextNode(getMethodAsString()));
		action.appendChild(method);
		
		Element preferenceMap = document.createElement("preferenceMap");
		action.appendChild(preferenceMap);
		this.preferenceMap.saveTo(document, preferenceMap);
	}

	/**
	 * Gets the method as a string
	 * 
	 * @return method as string
	 */
	private String getMethodAsString() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(method);
			oos.close();
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "ERROR WRITING METHOD AS STRING";
	}

	/** Read the object from Base64 string. */
	private static Object fromString(String s) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}
}
