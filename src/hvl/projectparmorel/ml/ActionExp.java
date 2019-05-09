package hvl.projectparmorel.ml;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ActionExp implements Serializable {

	Action action;
	Map<Integer, Integer> tagsDictionary = new HashMap<Integer, Integer>();
	
		public ActionExp() {
		super();
	}
	public ActionExp(Action action, Map<Integer, Integer> tagsDictionary) {
		super();
		this.action = action;
		this.tagsDictionary = tagsDictionary;
	}
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	public Map<Integer, Integer> getTagsDictionary() {
		return tagsDictionary;
	}
	public void setTagsDictionary(Map<Integer, Integer> tagsDictionary) {
		this.tagsDictionary = tagsDictionary;
	}
	@Override
	public String toString() {
		return "ActionExp [action=" + action + ", tagsDictionary=" + tagsDictionary + "]";
	}

	
}
