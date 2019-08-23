package hvl.projectparmorel.ml;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TagDictionary implements Serializable {
	private static final long serialVersionUID = 1L;
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
