package hvl.projectparmorel.knowledge;

import java.util.List;

/**
 * Represents the algorithms knowledge.
 * 
 * @author Angela Barriga Rodriguez
 * @author Magnus Marthinsen
 */
public class Knowledge {
	
	ActionDirectory actionDirectory;
	QTable qTable;
	
	public Knowledge() {
		actionDirectory = new ActionDirectory();
		qTable = new QTable();
	}
	
	/**
	 * Adds 20% of the scores set in the preferences to the QTable. 
	 * We only add 20 %, so we don't influence the scores to much. 
	 * This allows for new learnings to be acquired.
	 * 
	 * @param the preferences to influence. Only these preferences will be affected.
	 */
	public void influenceQTableFromPreferenceScores(List<Integer> preferences) {
		ErrorContextActionDirectory<Double> qTableDirectory = qTable.getQTableDirectory();
		ErrorContextActionDirectory<Action> preferenceScores = actionDirectory.getActionDirectory();
		qTableDirectory.influenceWeightsByPreferedScores(preferenceScores, preferences);
	}
	
	/**
	 * Gets the QTable
	 * 
	 * @return the QTable
	 */
	public QTable getQTable(){
		return qTable;
	}
	
	/**
	 * Gets the action directory
	 * 
	 * @return the action directory
	 */
	public ActionDirectory getActionDirectory(){
		return actionDirectory;
	}
}
