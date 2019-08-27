package hvl.projectparmorel.knowledge;

import java.util.HashMap;
import java.util.Map;

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
}

