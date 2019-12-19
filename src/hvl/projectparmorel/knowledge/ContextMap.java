package hvl.projectparmorel.knowledge;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import hvl.projectparmorel.general.Action;

import org.w3c.dom.Node;

class ContextMap {
	private final String XML_NODE_NAME = "context";
	private final String XML_ID_NAME = "id";
	/**
	 * A map containing the actions for the given context.
	 */
	private Map<Integer, ActionMap> actions;

	protected ContextMap() {
		actions = new HashMap<>();
	}

	protected ContextMap(Integer contextId, Action action) {
		this();
		actions.put(contextId, new ActionMap(action));
	}

	protected ContextMap(Element error) throws IOException {
		this();
		NodeList contextList = error.getElementsByTagName(XML_NODE_NAME);
		for(int i = 0; i < contextList.getLength(); i++) {
			Node context = contextList.item(i);
			if(context.getNodeType() == Node.ELEMENT_NODE) {
				Element contextElement = (Element) context;
				Integer contextId = Integer.parseInt(contextElement.getAttribute(XML_ID_NAME));
				ActionMap contextMap = new ActionMap(contextElement);
				actions.put(contextId, contextMap);
			} else {
				throw new IOException("Could not instantiate context from node " + context.getNodeName());
			}
		}
	}

	/**
	 * Checks that the provided context id is stored in the Context Map.
	 * 
	 * @param contextId
	 * @return true if the context ID exists in the map, false otherwise.
	 */
	protected boolean containsContextId(Integer contextId) {
		return actions.containsKey(contextId);
	}

	/**
	 * Checks that the provided action ID exists for the given context
	 * 
	 * @param contextId
	 * @param actionId
	 * @return true if the action ID is found for the given context, false otherwise.
	 */
	protected boolean containsActionForContext(int contextId, int actionId) {
		if (actions.containsKey(contextId)) {
			return actions.get(contextId).containsValue(actionId);
		} else {
			return false;
		}
	}

	/**
	 * Gets the optimal context and action ID to handle the specified error.
	 * 
	 * @return the location of highest value in the context map. If two are equal,
	 *         one of them is returned. If the set is empty, null is returned.
	 */
	protected Action getOptimalAction() {
		Set<Integer> contextIdSet = actions.keySet();
		Integer[] contextIds = new Integer[contextIdSet.size()];
		contextIds = contextIdSet.toArray(contextIds);

		if (contextIds.length > 0) {
			Integer optimalContextId = contextIds[0];
			Integer optimalActionId = actions.get(optimalContextId).getBestActionKey();
			Action optimalAction = actions.get(optimalContextId).getAction(optimalActionId);

			for (int i = 1; i < contextIds.length; i++) {
				Integer optimalActionIdForContext = actions.get(contextIds[i]).getBestActionKey();
				Action action = actions.get(contextIds[i]).getAction(optimalActionIdForContext);
				if (action.compareTo(optimalAction) > 0) {
					optimalContextId = contextIds[i];
					optimalActionId = optimalActionIdForContext;
					optimalAction = action;
				}
			}

			return optimalAction;
		}
		return null;
	}

	/**
	 * Gets the number of contexts
	 * 
	 * @return the number of contexts
	 */
	protected int getNumberOfContexts() {
		return actions.keySet().size();
	}

	/**
	 * Returns the number of actions stored in the current context.
	 * 
	 * @param contextId
	 * @return the number of actions stored.
	 */
	protected int getNumberOfActionsInContext(Integer contextId) {
		return actions.keySet().size();
	}

	/**
	 * Gets a random action in a random context
	 * 
	 * @return a random value
	 */
	protected Action getRandomActionInRandomContext() {
		Random randomGenerator = new Random();
		Integer[] contextIds = new Integer[actions.keySet().size()];
		contextIds = actions.keySet().toArray(contextIds);
		int randomContextIndex = randomGenerator.nextInt(contextIds.length);
		return actions.get(contextIds[randomContextIndex]).getRandomAction();
	}
	
	/**
	 * Gets the value for the specified error code, context id and action id.
	 * 
	 * @param contextId
	 * @param actionId
	 * @return the corresponding value
	 */
	protected Action getAction(Integer contextId, Integer actionId) {
		return actions.get(contextId).getAction(actionId);
	}

	/**
	 * Adds the value for the specified action in the specified context. If the
	 * context or action is in the hierarchy, they will be updated.
	 * 
	 * @param contextId
	 * @param action
	 */
	protected void addAction(Integer contextId, Action action) {
		if (actions.containsKey(contextId)) {
			actions.get(contextId).addAction(action);
		} else {
			actions.put(contextId, new ActionMap(action));
		}
	}

	/**
	 * Saves content to the document under the error element
	 * 
	 * @param document 
	 * @param error
	 */
	protected void saveTo(Document document, Element error) {
		for(Integer key : actions.keySet()) {
            Element context = document.createElement(XML_NODE_NAME);
            
            Attr contextId = document.createAttribute(XML_ID_NAME);
            contextId.setValue("" + key);
            context.setAttributeNode(contextId);
            
            actions.get(key).saveTo(document, context);
            error.appendChild(context);
		}
	}

	/**
	 * Sets all the weights to zero.
	 */
	protected void clearActionWeights() {
		for(ActionMap actions : actions.values()) {
			actions.clearWeights();
		}
	}

	/**
	 * Influences the action weights from the preferences and previous learning by the specified factor.
	 * 
	 * @param factor
	 * @param preferences 
	 */
	protected void influenceActionWeightsFromPreferencesBy(double factor, List<Integer> preferences) {
		for(ActionMap actions : actions.values()) {
			actions.influenceWeightsFromPreferencesBy(factor, preferences);
		}
	}
}
