import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Experience implements Serializable {

	private static final long serialVersionUID = 1L;
	Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> qTable = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>();
	Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> actionsDictionary = new HashMap<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>>();

	public Experience() {
		super();
	}

	public Experience(Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> qTable,
			Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> actionsDictionary) {
		super();
		this.qTable = qTable;
		this.actionsDictionary = actionsDictionary;
	}

	public Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> getqTable() {
		return qTable;
	}

	public void setqTable(Map<Integer, HashMap<Integer, HashMap<Integer, Double>>> qTable) {
		this.qTable = qTable;
	}

	public Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> getActionsDictionary() {
		return actionsDictionary;
	}

	public void setActionsDictionary(Map<Integer, HashMap<Integer, HashMap<Integer, ActionExp>>> actionsDictionary) {
		this.actionsDictionary = actionsDictionary;
	}




}
