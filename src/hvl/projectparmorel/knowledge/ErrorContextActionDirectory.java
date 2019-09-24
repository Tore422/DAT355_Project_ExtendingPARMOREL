package hvl.projectparmorel.knowledge;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class ErrorContextActionDirectory {
	public ErrorContextActionDirectory() {

	}

	/**
	 * Gets all the error codes
	 * 
	 * @return a Set containing all the error codes.
	 */
	public abstract Set<Integer> getAllErrorCodes();
	
	/**
	 * Gets the error map from the directory;
	 */
	protected abstract ErrorMap getErrorMap();
	
	/**
	 * Gets the optimal context and action ID to handle the specified error.
	 * 
	 * @param errorCode
	 * @return the location of highest value in the context map. If two are equal,
	 *         one of them is returned. If the set is empty, null is returned.
	 */
	protected abstract Action getOptimalActionForErrorCode(Integer errorCode);

	/**
	 * Checks that the provided value exists for the specified error code and context id
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the value exists for the specified error code and context ID, false otherwise.
	 */
	protected abstract boolean containsValueForErrorAndContext(int errorCode, int contextId, int actionId);
	
	/**
	 * Gets a random action for the specified error
	 * 
	 * @param errorCode
	 * @return a random action
	 */
	protected abstract Action getRandomActionForError(int errorCode);
	
	/**
	 * Gets the action for the specified error code, context id and action id.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @return the corresponding action
	 */
	protected abstract Action getAction(Integer errorCode, Integer contextId, Integer actionId);
	
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
	protected abstract void saveTo(Document document, Element root);

	/**
	 * Loads content from the specified document from the root element.
	 * 
	 * @param root
	 */
	protected abstract void loadFrom(Document document);

	/**
	 * Sets all the weights in the q-table to zero.
	 */
	protected abstract void clearWeights();

	/**
	 * Influences the weights in the q-table from the preferences and previous learning by the specified factor.
	 * 
	 * @param factor
	 * @param preferences 
	 */
	protected abstract void influenceWeightsFromPreferencesBy(double factor, List<Integer> preferences);
}