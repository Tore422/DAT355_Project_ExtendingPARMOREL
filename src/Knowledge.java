import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Knowledge implements Serializable {

	private static final long serialVersionUID = 1L;
	Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> qTable = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>();
	ArrayList<Integer> tags = new ArrayList<Integer>();
	Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> actionsDictionary = new HashMap<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>>();

	public Knowledge() {
		super();
	}

	public Knowledge(Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> qTable, ArrayList<Integer> tags,
			Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> actionsDictionary) {
		super();
		this.qTable = qTable;
		this.tags = tags;
		this.actionsDictionary = actionsDictionary;
	}

	public Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> getqTable() {
		return qTable;
	}

	public void setqTable(Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> qTable) {
		this.qTable = qTable;
	}

	public ArrayList<Integer> getTags() {
		return tags;
	}

	public void setTags(ArrayList<Integer> tags) {
		this.tags = tags;
	}

	public Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> getActionsDictionary() {
		return actionsDictionary;
	}

	public void setActionsDictionary(Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> actionsDictionary) {
		this.actionsDictionary = actionsDictionary;
	}




}
