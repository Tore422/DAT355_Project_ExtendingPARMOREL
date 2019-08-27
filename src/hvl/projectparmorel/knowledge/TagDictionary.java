package hvl.projectparmorel.knowledge;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TagDictionary {
	private Map<Integer, Integer> tagDictionary;
	
	public TagDictionary() {
		 tagDictionary = new HashMap<Integer, Integer>();
	}
	
	public TagDictionary(Map<Integer, Integer> tagDictionary) {
		this.tagDictionary = tagDictionary;
	}

	public Map<Integer, Integer> getTagDictionary() {
		return tagDictionary;
	}	
	
	/**
	 * Gets all the tag IDs.
	 * 
	 * @return all the tag IDs.
	 */
	public Set<Integer> getAllTagIds(){
		return tagDictionary.keySet();
	}
	
	/**
	 * Gets the tag for the corresponding tag id.
	 * @param tagId to get tag for
	 * @return the corresponding tag id
	 */
	public int getTagFor(Integer tagId) {
		return tagDictionary.get(tagId);
	}
}

