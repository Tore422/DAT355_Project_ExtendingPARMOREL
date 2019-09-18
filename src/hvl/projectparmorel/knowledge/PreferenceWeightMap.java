package hvl.projectparmorel.knowledge;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PreferenceWeightMap {
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
}