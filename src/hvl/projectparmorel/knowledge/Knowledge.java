package hvl.projectparmorel.knowledge;

import java.util.List;

/**
 * Represents the algorithms knowledge.
 * 
 * @author Angela Barriga Rodriguez
 * @author Magnus Marthinsen
 */
public class Knowledge {
	private QTable actionDirectory;
	
	public Knowledge() {
		actionDirectory = new QTable();
	}
	
	/**
	 * Adds 20% of the scores set in the preferences to the QTable. 
	 * We only add 20 %, so we don't influence the scores to much. 
	 * This allows for new learnings to be acquired.
	 * 
	 * @param the preferences to influence. Only these preferences will be affected.
	 */
	public void influenceQTableFromPreferenceScores(List<Integer> preferences) {
		ErrorContextActionDirectory<Action> preferenceScores = actionDirectory.getActionDirectory();
		preferenceScores.influenceWeightsByPreferedScores(preferenceScores, preferences);
	}
	
	/**
	 * Gets the action directory
	 * 
	 * @return the action directory
	 */
	public QTable getActionDirectory(){
		return actionDirectory;
	}
	
	/**
	 * Gets the optimal action for the specified error code.
	 * 
	 * @param errorCode
	 * @return the action for the specified error code with the highest weight.
	 */
	public Action getOptimalActionForErrorCode(Integer errorCode) {
		return actionDirectory.getOptimalActionForErrorCode(errorCode);
	}
}
