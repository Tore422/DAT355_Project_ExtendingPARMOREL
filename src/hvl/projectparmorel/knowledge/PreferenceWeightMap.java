package hvl.projectparmorel.knowledge;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PreferenceWeightMap {
	private final String XML_ID_NAME = "id";
	private final String XML_VALUE_NAME = "value";
	
	/**
	 * This is the map updated through the execution.
	 */
	private Map<Integer, Integer> executionPreferenceMap;
	/**
	 * This is the map updated at the end of each execution and stored.
	 */
	private Map<Integer, Integer> storedPreferenceMap;
	
	public PreferenceWeightMap() {
		 executionPreferenceMap = new HashMap<Integer, Integer>();
		 storedPreferenceMap = new HashMap<Integer, Integer>();
	}
	
	public PreferenceWeightMap(Map<Integer, Integer> preferenceMap) {
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

	public Map<Integer, Integer> getPreferenceMap() {
		return executionPreferenceMap;
	}	
	
	/**
	 * Gets all the preferences.
	 * 
	 * @return all the preferences.
	 */
	public Set<Integer> getAllPreferenceIds(){
		return executionPreferenceMap.keySet();
	}
	
	/**
	 * Gets the weight for the corresponding preference id.
	 * 
	 * @param preferenceId to get weight for
	 * @return the corresponding weight
	 */
	public int getWeightFor(Integer preferenceId) {
		return executionPreferenceMap.get(preferenceId);
	}

	/**
	 * Inserts the value for the specified preference id
	 * 
	 * @param preferenceId
	 * @param value
	 */
	protected void set(int preferenceId, int value) {
		executionPreferenceMap.put(preferenceId, value);
	}

	/**
	 * Checks if the map contains the specific preference id
	 * 
	 * @param preferenceId
	 * @return true if the dictionary contains the preference id
	 */
	public boolean contains(Integer preferenceId) {
		return executionPreferenceMap.containsKey(preferenceId);
	}

	/**
	 * Saves the preferences for the specified preference ID, and combines it with the old value if it exists.
	 * 
	 * @param preferenceId
	 */
	public void combineAndSavePreference(Integer preferenceId) {
		if(storedPreferenceMap.containsKey(preferenceId)) {
			storedPreferenceMap.put(preferenceId, storedPreferenceMap.get(preferenceId) + executionPreferenceMap.get(preferenceId));
		} else {
			storedPreferenceMap.put(preferenceId, executionPreferenceMap.get(preferenceId));
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