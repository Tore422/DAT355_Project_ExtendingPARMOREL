package hvl.projectparmorel.ml;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import hvl.projectparmorel.knowledge.Action;

public class ActionExp implements Serializable {

	private static final long serialVersionUID = 1L;
	
	Action action;
//	Map<Integer, Integer> tagsDictionary = new HashMap<Integer, Integer>();
	private TagDictionary tagDictionary;

	public ActionExp() {
		super();
		tagDictionary = new TagDictionary();
	}

	public ActionExp(Action action, Map<Integer, Integer> tagsDictionary) {
		super();
		this.action = action;
		tagDictionary = new TagDictionary(tagsDictionary);
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Map<Integer, Integer> getTagsDictionary() {
		return tagDictionary.getTagDictionary();
	}

	public Set<Integer> getAllTagIds(){
		return tagDictionary.getTagDictionary().keySet();
	}
	
	@Override
	public String toString() {
		return "ActionExp [action=" + action + ", tagsDictionary=" + tagDictionary + "]";
	}

}
