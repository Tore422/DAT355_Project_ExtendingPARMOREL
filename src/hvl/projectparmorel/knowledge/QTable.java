package hvl.projectparmorel.knowledge;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.reward.PreferenceOption;

public class QTable {
	ErrorContextActionDirectory qTable;

	protected QTable() {
		qTable = new HashErrorContextActionDirectory();
	}

	/**
	 * Checks that the provided error code is stored in the ErrorMap.
	 * 
	 * @param errorCode to check
	 * @return true if the errorCode is in the ErrorMap, false otherwise.
	 */
	public boolean containsErrorCode(Integer errorCode) {
		return qTable.getErrorMap().containsErrorCode(errorCode);
	}

	/**
	 * Checks that the provided action Id exists for the specified error code and
	 * context id
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the action ID exists for the specified error code and context
	 *         ID, false otherwise.
	 */
	public boolean containsActionForErrorAndContext(int errorCode, int contextId, int actionId) {
		return qTable.containsValueForErrorAndContext(errorCode, contextId, actionId);
	}

	/**
	 * Sets the weight for the specified action in the specified context for the
	 * specified error.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param weight
	 */
	public void setWeight(Integer errorCode, Integer contextId, Integer actionId, Double weight) {
		Action action = qTable.getAction(errorCode, contextId, actionId);
		action.setWeight(weight);
	}

	/**
	 * Gets the weight for the specified action for the specified context for the
	 * specified error.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return the weight
	 */
	public double getWeight(Integer errorCode, Integer contextId, Integer actionId) {
		return qTable.getAction(errorCode, contextId, actionId).getWeight();
	}

	/**
	 * Gets the qTable directory.
	 * 
	 * @return the qTalbe
	 */
	protected ErrorContextActionDirectory getActionDirectory() {
		return qTable;
	}

	/**
	 * Gets a random action for the specified error
	 * 
	 * @param errorCode
	 * @return a random action
	 */
	public Action getRandomActionForError(int errorCode) {
		return qTable.getRandomActionForError(errorCode);
	}

	/**
	 * Gets the tag dictionary for the specified action
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return the tag dictionary for the action
	 */
	public PreferenceWeightMap getTagDictionaryForAction(Integer errorCode, Integer contextId,
			Integer actionId) {
		Action action = getAction(errorCode, contextId, actionId);
		return action.getPreferenceMap();
	}

	/**
	 * Sets the value for the specified tag for the specified action in the
	 * specified context for the specified error.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param tag
	 * @param value
	 */
	public void setTagValueInTagDictionary(Integer errorCode, Integer contextId, Integer actionId, int tag, int value) {
		Action action = getAction(errorCode, contextId, actionId);
		action.getPreferenceMap().set(tag, value);
	}

	/**
	 * Adds action to the specified error code and context ID.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param action
	 */
	public void setAction(int errorCode, int contextId, Action action) {
		qTable.addAction(errorCode, contextId, action);
	}
	
	/**
	 * Gets the value for the specified error code, context id and action id.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return the corresponding value
	 */
	protected Action getAction(Integer errorCode, Integer contextId, Integer actionId) {
		return qTable.getAction(errorCode, contextId, actionId);
	}

	/**
	 * Gets the optimal context and action ID to handle the specified error.
	 * 
	 * @param errorCode
	 * @return the location of highest value in the context map. If two are equal,
	 *         one of them is returned. If the set is empty, null is returned.
	 */
	protected Action getOptimalActionForErrorCode(Integer errorCode) {
		return qTable.getOptimalActionForErrorCode(errorCode);
	}

	public void updateReward(AppliedAction errorAction, int contextId) {
		int errorCode = errorAction.getError().getCode();
		int actionId = errorAction.getAction().getId();
		
		if(qTable.containsValueForErrorAndContext(errorCode, contextId, actionId)) {
			Action actionToUpdate = errorAction.getAction();
			actionToUpdate.savePreferenceWeights();
		}
	}

	/**
	 * Saves content to the document under the root element
	 * 
	 * @param document 
	 * @param root
	 */
	protected void saveTo(Document document, Element root) {
		qTable.saveTo(document, root);	
	}

	/**
	 * Loads content from the specified document
	 * 
	 * @param document
	 */
	protected void loadFrom(Document document) {
		qTable.loadFrom(document);
	}

	/**
	 * Sets all the weights in the q-table to zero.
	 */
	protected void clearWeights() {
		qTable.clearWeights();
	}

	/**
	 * Influences the weights in the q-table from the preferences and previous learning by the specified factor.
	 * 
	 * @param factor
	 * @param preferences 
	 */
	protected void influenceWeightsFromPreferencesBy(double factor, List<PreferenceOption> preferences) {
		qTable.influenceWeightsFromPreferencesBy(factor, preferences);
	}
}
