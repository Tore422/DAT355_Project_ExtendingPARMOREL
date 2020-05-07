package no.hvl.projectparmorel.qlearning.knowledge;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import no.hvl.projectparmorel.qlearning.Action;
import no.hvl.projectparmorel.qlearning.reward.PreferenceOption;

import org.w3c.dom.Node;

class ErrorMap {
	private final String XML_NODE_NAME = "error";
	private final String XML_CODE_NAME = "code";
	/**
	 * A map containing the context for the given error codes.
	 */
	private Map<Integer, ContextMap> contexts;

	protected ErrorMap() {
		contexts = new HashMap<>();
	}

	/**
	 * Gets all the error codes
	 * 
	 * @return a Set containing all the error codes.
	 */
	protected Set<Integer> getAllErrorCodes() {
		return contexts.keySet();
	}

	/**
	 * Checks that the provided error code is stored in the ErrorMap.
	 * 
	 * @param errorCode to check
	 * @return true if the errorCode is in the ErrorMap, false otherwise.
	 */
	protected boolean containsErrorCode(Integer errorCode) {
		return contexts.containsKey(errorCode);
	}
	
	/**
	 * Checks that the provided action id is stored for the given error and context.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the action ID if found for the specified errorCode and contextId, false otherwise.
	 */
	protected boolean containsActionForErrorCodeAndContextId(int errorCode, int contextId, int actionId) {
		if(contexts.containsKey(errorCode)) {
			return contexts.get(errorCode).containsActionForContext(contextId, actionId);
		} else {
			return false;
		}
		
	}

	/**
	 * Gets the optimal context and action ID to handle the specified error.
	 * 
	 * @param errorCode
	 * @return the location of highest value in the context map. If two are equal, one of them is returned. If the set is empty, null is returned.
	 */
	protected Action getOptimalActionForErrorCode(Integer errorCode) {
		ContextMap contextForErrorCode = contexts.get(errorCode);
		return contextForErrorCode.getOptimalAction();
	}

	/**
	 * Gets the number of contexts that exists for a specified error.
	 * 
	 * @param errorCode
	 * @return the number of contexts for the error code
	 */
	protected int getNumberOfContextsForError(int errorCode) {
		return contexts.get(errorCode).getNumberOfContexts();
	}

	/**
	 * Gets a random action in a random context for the specified error code.
	 * 
	 * @param errorCode 
	 * @return a random context
	 */
	protected Action getRandomActionInRandomContextForError(int errorCode) {
		ContextMap contextsForError = contexts.get(errorCode);
		return contextsForError.getRandomActionInRandomContext();
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
		return contexts.get(errorCode).getAction(contextId, actionId);
	}
	
	/**
	 * Adds the value for the specified action in the specified context for the
	 * specified error. If the error, context or action is in the hierarchy,
	 * they will be updated.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param value
	 */
	protected void addAction(Integer errorCode, Integer contextId, Action action) {
		if (contexts.containsKey(errorCode)) {
			contexts.get(errorCode).addAction(contextId, action);
		} else {
			contexts.put(errorCode, new ContextMap(contextId, action));
		}
	}

	/**
	 * Saves content to the document under the root element
	 * 
	 * @param document 
	 * @param root
	 */
	protected void saveTo(Document document, Element root) {
		for(Integer key : contexts.keySet()) {
            Element error = document.createElement(XML_NODE_NAME);
            
            Attr errorCode = document.createAttribute(XML_CODE_NAME);
            errorCode.setValue("" + key);
            error.setAttributeNode(errorCode);
            
            contexts.get(key).saveTo(document, error);
            root.appendChild(error);
		}	
	}

	/**
	 * Loads the content from the specified document.
	 * 
	 * @param document
	 * @throws IOException 
	 */
	protected void loadFrom(Document document) throws IOException {
		NodeList errorList = document.getElementsByTagName(XML_NODE_NAME);
		for (int i = 0; i < errorList.getLength(); i++) {
			Node error = errorList.item(i);
			if(error.getNodeType() == Node.ELEMENT_NODE) {
				Element errorElement = (Element) error;
				Integer errorCode = Integer.parseInt(errorElement.getAttribute(XML_CODE_NAME));
				ContextMap contextMap = new ContextMap(errorElement);
				contexts.put(errorCode, contextMap);
			} else {
				throw new IOException("Could not instantiate error from node " + error.getNodeName());
			}
		}
	}

	/**
	 * Sets all the weights in the q-table to zero.
	 */
	protected void clearActionWeights() {
		for(ContextMap context : contexts.values()) {
			context.clearActionWeights();
		}
	}

	/**
	 * Influences the action weights from the preferences and previous learning by the specified factor.
	 * 
	 * @param factor
	 * @param preferences 
	 */
	protected void influenceActionWeightsFromPreferencesBy(double factor, List<PreferenceOption> preferences) {
		for(ContextMap context : contexts.values()) {
			context.influenceActionWeightsFromPreferencesBy(factor, preferences);
		}
	}
}
