package hvl.projectparmorel.knowledge;

public class QTable {
	ErrorContextActionDirectory<Double> qTable;

	protected QTable() {
		qTable = new HashErrorContextActionDirectory<>();
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
	 * Checks that the specified error code and context ID contains the specified
	 * action ID
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @return true if the action ID is found for the specified error and context,
	 *         false otherwise
	 */
	public boolean containsActionIdForErrorCodeAndContextId(int errorCode, int contextId, int actionId) {
		return qTable.getErrorMap().containsValueForErrorCodeAndContextId(errorCode, contextId, actionId);
	}

	/**
	 * Sets the weight for the specified action in the specified context for the
	 * specified error. If the error, context or action is not in the hierarchy,
	 * they will be added.
	 * 
	 * @param errorCode
	 * @param contextId
	 * @param actionId
	 * @param weight
	 */
	public void setWeight(Integer errorCode, Integer contextId, Integer actionId, Double weight) {
		qTable.setValue(errorCode, contextId, actionId, weight);
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
		return qTable.getValue(errorCode, contextId, actionId);
	}

	/**
	 * Gets the qTable directory.
	 * 
	 * @return the qTalbe
	 */
	protected ErrorContextActionDirectory<Double> getQTableDirectory() {
		return qTable;
	}

	/**
	 * Gets the optimal context and action ID to handle the specified error.
	 * 
	 * @param errorCode
	 * @return the location of highest value in the context map. If two are equal,
	 *         one of them is returned. If the set is empty, null is returned.
	 */
	protected ActionLocation getOptimalActionIndexForErrorCode(Integer errorCode) {
		return qTable.getOptimalActionIndexForErrorCode(errorCode);
	}
}
