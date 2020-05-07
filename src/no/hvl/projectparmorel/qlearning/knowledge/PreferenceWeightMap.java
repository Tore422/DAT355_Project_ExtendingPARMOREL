package no.hvl.projectparmorel.qlearning.knowledge;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import no.hvl.projectparmorel.qlearning.reward.PreferenceOption;

public class PreferenceWeightMap {
	private final String XML_ID_NAME = "id";
	private final String XML_VALUE_NAME = "value";
	
	/**
	 * This is the map updated through the execution.
	 */
	private Map<PreferenceOption, Integer> executionPreferenceMap;
	/**
	 * This is the map updated at the end of each execution and stored.
	 */
	private Map<Integer, Integer> storedPreferenceMap;
	
	public PreferenceWeightMap() {
		 executionPreferenceMap = new HashMap<PreferenceOption, Integer>();
		 storedPreferenceMap = new HashMap<Integer, Integer>();
	}
	
	public PreferenceWeightMap(Map<PreferenceOption, Integer> preferenceMap) {
		this.executionPreferenceMap = preferenceMap;
		storedPreferenceMap = new HashMap<Integer, Integer>();
	}

	public PreferenceWeightMap(NodeList preferenceList) throws IOException {
		this();
		for(int i = 0; i < preferenceList.getLength(); i++) {
			Node context = preferenceList.item(i);
			if(context.getNodeType() == Node.ELEMENT_NODE) {
				Element preferenceElement = (Element) context;
				String preferenceIdAsString = preferenceElement.getAttribute(XML_ID_NAME);
				if(preferenceIdAsString.length() > 0) {
					Integer preferenceId = Integer.parseInt(preferenceIdAsString);
					Integer value = Integer.parseInt(preferenceElement.getAttribute(XML_VALUE_NAME));
					storedPreferenceMap.put(preferenceId, value);
				}
			} else {
				throw new IOException("Could not instantiate preference map from " + context.getNodeName());
			}
		}
	}

	public Map<PreferenceOption, Integer> getPreferenceMap() {
		return executionPreferenceMap;
	}	
	
	/**
	 * Gets all the preferences.
	 * 
	 * @return all the preferences.
	 */
	public Set<PreferenceOption> getAllPreferenceIds(){
		return executionPreferenceMap.keySet();
	}
	
	/**
	 * Gets the weight for the corresponding preference id.
	 * 
	 * @param preferenceId to get weight for
	 * @return the corresponding weight
	 */
	public int getWeightFor(PreferenceOption preference) {
		return executionPreferenceMap.get(preference);
	}

	/**
	 * Inserts the value for the specified preference id
	 * 
	 * @param preferenceId
	 * @param value
	 */
	protected void set(int preferenceId, int value) {
		executionPreferenceMap.put(PreferenceOption.valueOfID(preferenceId), value);
	}

	/**
	 * Checks if the map contains the specific preference id
	 * 
	 * @param preferenceId
	 * @return true if the dictionary contains the preference id
	 */
	public boolean contains(PreferenceOption preference) {
		return executionPreferenceMap.containsKey(preference);
	}

	/**
	 * Saves the preferences for the specified preference ID, and combines it with the old value if it exists.
	 * 
	 * @param preferenceId
	 */
	public void combineAndSavePreference(PreferenceOption preference) {
		if(storedPreferenceMap.containsKey(preference.id)) {
			storedPreferenceMap.put(preference.id, storedPreferenceMap.get(preference.id) + executionPreferenceMap.get(preference));
		} else {
			storedPreferenceMap.put(preference.id, executionPreferenceMap.get(preference));
		}
	}

	/**
	 * Saves the content to the document under preferenceMap
	 * @param document
	 * @param preferenceMap
	 */
	public void saveTo(Document document, Element preferenceMap) {
		for(Integer key : storedPreferenceMap.keySet()) {
			Element preference = document.createElement("preference");
			preferenceMap.appendChild(preference);
			
			Attr preferenceId = document.createAttribute(XML_ID_NAME);
			preferenceId.setValue("" + key);
            preference.setAttributeNode(preferenceId);
			
			Attr value = document.createAttribute(XML_VALUE_NAME);
            value.setValue("" + storedPreferenceMap.get(key));
            preference.setAttributeNode(value);
		}
	}
}