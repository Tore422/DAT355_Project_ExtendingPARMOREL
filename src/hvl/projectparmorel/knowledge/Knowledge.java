package hvl.projectparmorel.knowledge;

import java.util.List;
import java.util.Set;

/**
 * Represents the algorithms knowledge.
 * 
 * @author Angela Barriga Rodriguez
 * @author Magnus Marthinsen
 */
public class Knowledge {
	ErrorContextActionDirectory<Double> qTable;
	ErrorContextActionDirectory<Action> preferenceScores;
	
	public Knowledge() {
		qTable = new HashErrorContextActionDirectory<>();
		preferenceScores = new HashErrorContextActionDirectory<>();
	}
	
	/**
	 * Adds 20% of the scores set in the preferences to the QTable. 
	 * We only add 20 %, so we don't influence the scores to much. 
	 * This allows for new learnings to be acquired.
	 * 
	 * @param the preferences to influence. Only these preferences will be affected.
	 */
	public void influenceQTableFromPreferenceScores(List<Integer> preferences) {
		qTable.influenceWeightsByPreferedScores(preferenceScores, preferences);
	}
	
	public ErrorContextActionDirectory<Double> getQTable(){
		return qTable;
	}
	
	public ErrorContextActionDirectory<Action> getPreferenceScores(){
		return preferenceScores;
	}
}
