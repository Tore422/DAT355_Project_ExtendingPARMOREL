package hvl.projectparmorel.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import hvl.projectparmorel.knowledge.PreferenceWeightMap;
import hvl.projectparmorel.modelrepair.SerializableMethod;
import hvl.projectparmorel.reward.PreferenceOption;

/**
 * 
 * @author Angela Barriga Rodriguez - 2019, abar@hvl.no
 * @author Magnus Marthinsen
 * 
 *         Western Norway University of Applied Sciences Bergen - Norway
 */
public class Action implements Comparable<Action> {
	private final String XML_CODE_NAME = "code";
	private final String XML_WEIGHT_NAME = "weight";
	private final String XML_MESSAGE_NAME = "message";
	private final String XML_HIERARCHY_NAME = "hierarchy";
	private final String XML_METHOD_NAME = "method";
	private final String XML_PREFERENCEMAP_NAME = "preferenceMap";

	private PreferenceWeightMap preferenceMap;
	private int code;
	private String message;
	private SerializableMethod method;
	private int hierarchy;
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
	public Action(int code, String message, SerializableMethod method, int hierarchy) {
		this();
		this.code = code;
		this.message = message;
		this.method = method;
		this.hierarchy = hierarchy;
	}

	public Action(Element action) throws IOException {
		if (action.getNodeType() == Node.ELEMENT_NODE) {
			Element actionElement = action;
			code = Integer.parseInt(actionElement.getAttribute(XML_CODE_NAME));
			weight = Double.parseDouble(actionElement.getElementsByTagName(XML_WEIGHT_NAME).item(0).getTextContent());
			message = actionElement.getElementsByTagName(XML_MESSAGE_NAME).item(0).getTextContent();
			hierarchy = Integer
					.parseInt(actionElement.getElementsByTagName(XML_HIERARCHY_NAME).item(0).getTextContent());
			method = getMethodFromString(actionElement.getElementsByTagName(XML_METHOD_NAME).item(0).getTextContent());
			preferenceMap = new PreferenceWeightMap(actionElement.getElementsByTagName(XML_PREFERENCEMAP_NAME));
		} else {
			throw new IOException("Could not instantiate action from node " + action.getNodeName());
		}
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
					&& otherAction.getMethod().equals(method) && otherAction.getHierarchy() == hierarchy;
		}
		return false;
	}

	/**
	 * Compares actions based on weights
	 */
	@Override
	public int compareTo(Action otherAction) {
		Double thisWeight = weight;
		Double otherWeight = otherAction.getWeight();
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

	public void savePreferenceWeights() {
		for (PreferenceOption preference : preferenceMap.getAllPreferenceIds()) {
			preferenceMap.combineAndSavePreference(preference);
		}

	}

	public void saveTo(Document document, Element action) {
		Attr code = document.createAttribute(XML_CODE_NAME);
		code.setValue("" + this.code);
		action.setAttributeNode(code);

		Element weight = document.createElement(XML_WEIGHT_NAME);
		weight.appendChild(document.createTextNode("" + this.weight));
		action.appendChild(weight);

		Element message = document.createElement(XML_MESSAGE_NAME);
		message.appendChild(document.createTextNode(this.message));
		action.appendChild(message);

		Element hierarchy = document.createElement(XML_HIERARCHY_NAME);
		hierarchy.appendChild(document.createTextNode("" + this.hierarchy));
		action.appendChild(hierarchy);

		Element method = document.createElement(XML_METHOD_NAME);
		method.appendChild(document.createTextNode(getMethodAsString()));
		action.appendChild(method);

		Element preferenceMap = document.createElement(XML_PREFERENCEMAP_NAME);
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

	/**
	 * Read the object from Base64 string.
	 * 
	 * @throws IOException
	 */
	private SerializableMethod getMethodFromString(String methodAsString) throws IOException {
		Object object = null;
		try {
			byte[] data = Base64.getDecoder().decode(methodAsString);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			object = ois.readObject();
			ois.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (object instanceof SerializableMethod) {
			return (SerializableMethod) object;
		}
		return null;
	}

	/**
	 * Influences the weights from the preferences and previous learning by the
	 * specified factor.
	 * 
	 * @param factor
	 * @param preferences
	 */
	public void influenceWeightFromPreferencesBy(double factor, List<PreferenceOption> preferences) {
		for (PreferenceOption preference : preferenceMap.getAllPreferenceIds()) {
			if (preferences.contains(preference)) {
				int oldPreferenceValue = preferenceMap.getWeightFor(preference);
				weight += oldPreferenceValue * factor;
			}
		}
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
		return hierarchy;
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

	public void setCode(int code) {
		this.code = code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public PreferenceWeightMap getPreferenceMap() {
		return preferenceMap;
	}
}
