package hvl.projectparmorel.knowledge;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.reward.PreferenceOption;

public interface ErrorContextActionDirectory {
	/**
	 * Gets all the error codes
	 * 
	 * @return a Set containing all the error codes.
	 */
	public abstract Set<Integer> getAllErrorCodes();
	
	/**
	 * Gets the error map from the directory.
	 * 
	 * WARNING: Do not make any changes to the error map. All changes should be made through the {@link QTable hvl.projectparmorel.knowledge.QTable}-class.
	 */
	abstract ErrorMap getErrorMap();
	
	/**
	 * Gets the optimal context and action ID to handle the specified error.
	 * 
	 * @param errorCode
	 * @return the location of highest value in the context map. If two are equal,
	 *         one of them is returned. If the set is empty, null is returned.
	 */
	abstract Action getOptimalActionForErrorCode(Integer errorCode);

	/**
	 * Checks that the provided value exists for the specified error code and context id
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the value exists for the specified error code and context ID, false otherwise.
	 */
	abstract boolean containsValueForErrorAndContext(int errorCode, int contextId, int actionId);
	
	/**
	 * Gets a random action for the specified error
	 * 
	 * @param errorCode
	 * @return a random action
	 */
	abstract Action getRandomActionForError(int errorCode);
	
	/**
	 * Gets the action for the specified error code, context id and action id.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @return the corresponding action
	 */
	abstract Action getAction(Integer errorCode, Integer contextId, Integer actionId);
	
	/**
	 * Adds the value for the specified action in the specified context for the
	 * specified error. If the error, context or action is in the hierarchy,
	 * they will be updated.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param value
	 */
	public abstract void addAction(Integer errorCode, Integer contextId, Action action);

	/**
	 * Saves content to the document under the root element
	 * 
	 * @param document 
	 * @param root
	 */
	abstract void saveTo(Document document, Element root);

	/**
	 * Loads content from the specified document from the root element.
	 * 
	 * @param root
	 */
	abstract void loadFrom(Document document);

	/**
	 * Sets all the weights in the q-table to zero.
	 * 
	 * WARNING: This method is not intended to be called from outside the package. All changes to the q-table should be made through the {@link QTable hvl.projectparmorel.knowledge.QTable}-class.
	 */
	abstract void clearWeights();

	/**
	 * Influences the weights in the q-table from the preferences and previous learning by the specified factor.
	 * 
	 * WARNING: This method is not intended to be called from outside the package. All changes to the q-table should be made through the {@link QTable hvl.projectparmorel.knowledge.QTable}-class.
	 * 
	 * @param factor
	 * @param preferences 
	 */
	abstract void influenceWeightsFromPreferencesBy(double factor, List<PreferenceOption> preferences);
}